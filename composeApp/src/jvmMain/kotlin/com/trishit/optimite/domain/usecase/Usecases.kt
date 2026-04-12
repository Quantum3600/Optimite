package com.trishit.optimite.domain.usecase

import com.trishit.optimite.domain.model.*
import com.trishit.optimite.domain.repository.MemoryRepository
import com.trishit.optimite.domain.repository.ProcessRepository
import com.trishit.optimite.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveSystemStatsUseCase(
    private val memoryRepository: MemoryRepository,
    private val storageRepository: StorageRepository
) {
    operator fun invoke(): Flow<SystemSnapshot> =
        memoryRepository.observeMemoryInfo()
            .combine(storageRepository.observeStorageInfo()) { mem, stor ->
                SystemSnapshot(memory = mem, storage = stor)
            }
}

class OptimizeMemoryUseCase(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(): OptimizationResult {
        val before = memoryRepository.getMemoryInfo()
        val gcResult = memoryRepository.requestGarbageCollection()
        val cacheResult = memoryRepository.clearCaches()
        val after = memoryRepository.getMemoryInfo()
        val freed = before.usedMemoryMb - after.usedMemoryMb
        return OptimizationResult(
            freedMemoryMb = freed.coerceAtLeast(0L),
            gcRunCount = gcResult.gcRunCount + cacheResult.gcRunCount,
            message = "Optimization complete. Freed ~${freed.coerceAtLeast(0L)} MB"
        )
    }
}

class GetTopProcessesUseCase(
    private val processRepository: ProcessRepository
) {
    fun observe(limit: Int = 10): Flow<List<ProcessInfo>> =
        processRepository.observeTopProcesses(limit)
}

class CleanStorageUseCase(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(): Long = storageRepository.cleanTempFiles()
}