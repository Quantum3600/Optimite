package com.trishit.optimite.domain.model
data class MemoryInfo(
    val totalMemoryMb: Long,
    val usedMemoryMb: Long,
    val freeMemoryMb: Long,
    val maxMemoryMb: Long,
    val heapUsedMb: Long,
    val heapMaxMb: Long,
    val nonHeapUsedMb: Long,
    val gcCount: Long,
    val gcTimeMs: Long,
    val usagePercent: Float
)

data class StorageInfo(
    val drives: List<DriveInfo>
)

data class DriveInfo(
    val path: String,
    val name: String,
    val totalSpaceGb: Double,
    val usableSpaceGb: Double,
    val usedSpaceGb: Double,
    val usagePercent: Float,
    val type: DriveType
)

enum class DriveType { SYSTEM, REMOVABLE, NETWORK, UNKNOWN }

data class ProcessInfo(
    val pid: Long,
    val name: String,
    val memoryMb: Long,
    val cpuPercent: Double,
    val status: String
)

data class OptimizationResult(
    val freedMemoryMb: Long,
    val gcRunCount: Int,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SystemSnapshot(
    val memory: MemoryInfo,
    val storage: StorageInfo,
    val timestamp: Long = System.currentTimeMillis()
)