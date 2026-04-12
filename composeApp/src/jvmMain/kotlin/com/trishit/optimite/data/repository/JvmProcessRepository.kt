package com.trishit.optimite.data.repository

import com.trishit.optimite.domain.model.ProcessInfo
import com.trishit.optimite.domain.repository.ProcessRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.management.ManagementFactory

class JvmProcessRepository : ProcessRepository {

    private fun getProcesses(limit: Int): List<ProcessInfo> {
        return try {
            val os = System.getProperty("os.name").lowercase()
            when {
                os.contains("win") -> getWindowsProcesses(limit)
                os.contains("linux") || os.contains("mac") -> getUnixProcesses(limit)
                else -> getJvmSelfProcess()
            }
        } catch (_: Exception) {
            getJvmSelfProcess()
        }
    }

    private fun getWindowsProcesses(limit: Int): List<ProcessInfo> {
        val result = mutableListOf<ProcessInfo>()
        try {
            val process = Runtime.getRuntime().exec(
                arrayOf("tasklist", "/fo", "csv", "/nh")
            )
            process.inputStream.bufferedReader().use { reader ->
                reader.lineSequence()
                    .take(limit + 1)
                    .forEach { line ->
                        val parts = line.split(",").map { it.trim('"', ' ') }
                        if (parts.size >= 5) {
                            val memStr = parts[4].replace(",", "").replace(" K", "").trim()
                            val memKb = memStr.toLongOrNull() ?: 0L
                            result.add(
                                ProcessInfo(
                                    pid = parts[1].toLongOrNull() ?: 0,
                                    name = parts[0],
                                    memoryMb = memKb / 1024,
                                    cpuPercent = 0.0,
                                    status = parts[3]
                                )
                            )
                        }
                    }
            }
            process.destroy()
        } catch (_: Exception) { /* fallback */ }
        return result.sortedByDescending { it.memoryMb }.take(limit)
    }

    private fun getUnixProcesses(limit: Int): List<ProcessInfo> {
        val result = mutableListOf<ProcessInfo>()
        try {
            val process = Runtime.getRuntime().exec(
                arrayOf("ps", "aux", "--sort=-%mem")
            )
            process.inputStream.bufferedReader().use { reader ->
                reader.lineSequence()
                    .drop(1) // skip header
                    .take(limit)
                    .forEach { line ->
                        val parts = line.trim().split("\\s+".toRegex())
                        if (parts.size >= 11) {
                            result.add(
                                ProcessInfo(
                                    pid = parts[1].toLongOrNull() ?: 0,
                                    name = parts.drop(10).joinToString(" ").take(40),
                                    memoryMb = (parts[5].toLongOrNull() ?: 0L) / 1024,
                                    cpuPercent = parts[2].toDoubleOrNull() ?: 0.0,
                                    status = parts[7]
                                )
                            )
                        }
                    }
            }
            process.destroy()
        } catch (_: Exception) { /* fallback */ }
        return result.take(limit)
    }

    private fun getJvmSelfProcess(): List<ProcessInfo> {
        val rt = Runtime.getRuntime()
        val used = (rt.totalMemory() - rt.freeMemory()) / 1_048_576
        val pid = try {
            ManagementFactory.getRuntimeMXBean().name.split("@")[0].toLong()
        } catch (_: Exception) { 0L }
        return listOf(
            ProcessInfo(
                pid = pid,
                name = "MemoryOptimizer (current JVM)",
                memoryMb = used,
                cpuPercent = 0.0,
                status = "Running"
            )
        )
    }

    override fun observeTopProcesses(limit: Int): Flow<List<ProcessInfo>> = flow {
        while (true) {
            emit(getProcesses(limit))
            delay(3000)
        }
    }

    override suspend fun getTopProcesses(limit: Int): List<ProcessInfo> = getProcesses(limit)
}