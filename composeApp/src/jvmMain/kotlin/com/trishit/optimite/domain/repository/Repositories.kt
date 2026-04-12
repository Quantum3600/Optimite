package com.trishit.optimite.domain.repository

import com.trishit.optimite.domain.model.DriveInfo
import com.trishit.optimite.domain.model.MemoryInfo
import com.trishit.optimite.domain.model.OptimizationResult
import com.trishit.optimite.domain.model.ProcessInfo
import com.trishit.optimite.domain.model.StorageInfo
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    fun observeMemoryInfo(): Flow<MemoryInfo>
    suspend fun getMemoryInfo(): MemoryInfo
    suspend fun requestGarbageCollection(): OptimizationResult
    suspend fun clearCaches(): OptimizationResult
}

interface StorageRepository {
    fun observeStorageInfo(): Flow<StorageInfo>
    suspend fun getStorageInfo(): StorageInfo
    suspend fun getDriveInfo(path: String): DriveInfo?
    suspend fun cleanTempFiles(): Long // returns freed bytes
}

interface ProcessRepository {
    fun observeTopProcesses(limit: Int = 10): Flow<List<ProcessInfo>>
    suspend fun getTopProcesses(limit: Int = 10): List<ProcessInfo>
}