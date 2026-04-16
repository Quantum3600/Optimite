package com.trishit.optimite.data.repository

import com.trishit.optimite.domain.model.ProcessInfo
import com.trishit.optimite.domain.repository.ProcessRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import kotlin.streams.toList

class JvmProcessRepository : ProcessRepository {

    private suspend fun getProcesses(limit: Int): List<ProcessInfo> = withContext(Dispatchers.IO) {
        val os = System.getProperty("os.name").lowercase()
        val processList = try {
            when {
                os.contains("win") -> getWindowsProcesses()
                os.contains("linux") || os.contains("mac") -> getUnixProcesses()
                else -> getJvmSelfProcess()
            }
        } catch (e: Exception) {
            getJvmSelfProcess()
        }

        processList.sortedByDescending { it.memoryMb }.take(limit)
    }

    private fun getWindowsProcesses(): List<ProcessInfo> {
        val metrics = mutableMapOf<Long, Pair<Double, Long>>() // PID -> (CPU, MemoryBytes)
        
        // 1. Try to get metrics from PowerShell (most accurate for CPU/Private Memory)
        try {
            val script = "Get-CimInstance Win32_PerfFormattedData_PerfProc_Process | Select-Object IDProcess, PercentProcessorTime, WorkingSetPrivate | ForEach-Object { \"{0}|{1}|{2}\" -f ${'$'}_.IDProcess, ${'$'}_.PercentProcessorTime, ${'$'}_.WorkingSetPrivate }"
            val process = ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", script).start()
            
            process.inputStream.bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    val parts = line.split("|")
                    if (parts.size >= 3) {
                        val pid = parts[0].toLongOrNull() ?: return@forEachLine
                        val cpu = parts[1].replace(",", ".").toDoubleOrNull() ?: 0.0
                        val mem = parts[2].toLongOrNull() ?: 0L
                        if (pid > 0) metrics[pid] = Pair(cpu, mem)
                    }
                }
            }
            process.waitFor(3, TimeUnit.SECONDS)
            process.destroy()
        } catch (_: Exception) { }

        // 2. Fallback to tasklist if PowerShell metrics are empty or failed
        if (metrics.isEmpty()) {
            try {
                val process = ProcessBuilder("tasklist", "/fo", "csv", "/nh").start()
                process.inputStream.bufferedReader().use { reader ->
                    reader.forEachLine { line ->
                        val parts = line.split("\",\"").map { it.trim('"', ' ') }
                        if (parts.size >= 5) {
                            val pid = parts[1].toLongOrNull() ?: return@forEachLine
                            val memKb = parts[4].filter { it.isDigit() }.toLongOrNull() ?: 0L
                            metrics[pid] = Pair(0.0, memKb * 1024)
                        }
                    }
                }
                process.waitFor(2, TimeUnit.SECONDS)
                process.destroy()
            } catch (_: Exception) { }
        }

        // 3. Use ProcessHandle to get a consistent list of all processes and names
        // This ensures we "capture all processes" as requested.
        return ProcessHandle.allProcesses().map { handle ->
            val pid = handle.pid()
            val name = handle.info().command().map { File(it).name }.orElse("Unknown")
            val metric = metrics[pid]
            
            ProcessInfo(
                pid = pid,
                name = name,
                memoryMb = (metric?.second ?: 0L) / 1_048_576,
                cpuPercent = metric?.first ?: 0.0,
                status = if (handle.isAlive) "Running" else "Stopped"
            )
        }.toList()
    }

    private fun getUnixProcesses(): List<ProcessInfo> {
        val result = mutableListOf<ProcessInfo>()
        try {
            val process = ProcessBuilder("ps", "aux").start()
            process.inputStream.bufferedReader().use { reader ->
                reader.lineSequence().drop(1).forEach { line ->
                    val parts = line.trim().split("\\s+".toRegex())
                    if (parts.size >= 11) {
                        result.add(
                            ProcessInfo(
                                pid = parts[1].toLongOrNull() ?: 0,
                                name = parts.drop(10).joinToString(" ").take(40),
                                memoryMb = (parts[5].toLongOrNull() ?: 0L) / 1024,
                                cpuPercent = parts[2].replace(",", ".").toDoubleOrNull() ?: 0.0,
                                status = parts[7]
                            )
                        )
                    }
                }
            }
            process.waitFor(3, TimeUnit.SECONDS)
            process.destroy()
        } catch (_: Exception) { }
        return result
    }

    private fun getJvmSelfProcess(): List<ProcessInfo> {
        val rt = Runtime.getRuntime()
        val used = (rt.totalMemory() - rt.freeMemory()) / 1_048_576
        val pid = ProcessHandle.current().pid()
        return listOf(
            ProcessInfo(
                pid = pid,
                name = "Optimite (Self)",
                memoryMb = used,
                cpuPercent = 0.0,
                status = "Running"
            )
        )
    }

    override fun observeTopProcesses(limit: Int): Flow<List<ProcessInfo>> = flow {
        while (true) {
            try {
                emit(getProcesses(limit))
            } catch (e: Exception) {
                emit(getJvmSelfProcess())
            }
            delay(3000)
        }
    }

    override suspend fun getTopProcesses(limit: Int): List<ProcessInfo> = getProcesses(limit)
}