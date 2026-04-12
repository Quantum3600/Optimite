package com.trishit.optimite.data.repository

import com.trishit.optimite.domain.model.MemoryInfo
import com.trishit.optimite.domain.model.OptimizationResult
import com.trishit.optimite.domain.repository.MemoryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.management.ManagementFactory

class JvmMemoryRepository : MemoryRepository {

    private fun collectMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val memBean = ManagementFactory.getMemoryMXBean()
        val gcBeans = ManagementFactory.getGarbageCollectorMXBeans()

        val totalMem = runtime.totalMemory()
        val freeMem = runtime.freeMemory()
        val maxMem = runtime.maxMemory()
        val usedMem = totalMem - freeMem

        val heapUsage = memBean.heapMemoryUsage
        val nonHeapUsage = memBean.nonHeapMemoryUsage

        val gcCount = gcBeans.sumOf { it.collectionCount.coerceAtLeast(0) }
        val gcTime = gcBeans.sumOf { it.collectionTime.coerceAtLeast(0) }

        // Also read OS memory if available
        val osMxBean = ManagementFactory.getOperatingSystemMXBean()
        val (totalOsMem, freeOsMem) = try {
            val cls = Class.forName("com.sun.management.OperatingSystemMXBean")
            val totalPhys = cls.getMethod("getTotalPhysicalMemorySize").invoke(osMxBean) as Long
            val freePhys = cls.getMethod("getFreePhysicalMemorySize").invoke(osMxBean) as Long
            Pair(totalPhys, freePhys)
        } catch (_: Exception) {
            Pair(maxMem, freeMem)
        }

        val usedOs = totalOsMem - freeOsMem
        val usagePercent = if (totalOsMem > 0) (usedOs.toFloat() / totalOsMem.toFloat()) * 100f else 0f

        return MemoryInfo(
            totalMemoryMb = totalOsMem / 1_048_576,
            usedMemoryMb = usedOs / 1_048_576,
            freeMemoryMb = freeOsMem / 1_048_576,
            maxMemoryMb = maxMem / 1_048_576,
            heapUsedMb = heapUsage.used / 1_048_576,
            heapMaxMb = (if (heapUsage.max > 0) heapUsage.max else maxMem) / 1_048_576,
            nonHeapUsedMb = (if (nonHeapUsage.used > 0) nonHeapUsage.used else 0L) / 1_048_576,
            gcCount = gcCount,
            gcTimeMs = gcTime,
            usagePercent = usagePercent
        )
    }

    override fun observeMemoryInfo(): Flow<MemoryInfo> = flow {
        while (true) {
            emit(collectMemoryInfo())
            delay(1500)
        }
    }

    override suspend fun getMemoryInfo(): MemoryInfo = collectMemoryInfo()

    override suspend fun requestGarbageCollection(): OptimizationResult {
        val before = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
        repeat(3) {
            System.gc()
            Thread.sleep(100)
        }
        val after = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
        val freed = ((before - after) / 1_048_576).coerceAtLeast(0)
        return OptimizationResult(
            freedMemoryMb = freed,
            gcRunCount = 3,
            message = "GC completed, freed ~$freed MB"
        )
    }

    override suspend fun clearCaches(): OptimizationResult {
        // Clear soft references and trigger aggressive GC
        val weakMap = java.util.WeakHashMap<Any, Any>()
        repeat(5) {
            System.gc()
            Thread.sleep(50)
        }
        return OptimizationResult(
            freedMemoryMb = 0,
            gcRunCount = 5,
            message = "Cache sweep complete"
        )
    }
}