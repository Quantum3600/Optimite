package com.trishit.optimite.data.repository

import com.trishit.optimite.domain.model.DriveInfo
import com.trishit.optimite.domain.model.DriveType
import com.trishit.optimite.domain.model.StorageInfo
import com.trishit.optimite.domain.repository.StorageRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.nio.file.FileSystems

class JvmStorageRepository : StorageRepository {

    private fun collectStorageInfo(): StorageInfo {
        val fileSystem = FileSystems.getDefault()
        val drives = fileSystem.rootDirectories.mapNotNull { path ->
            try {
                val root = File(path.toString())
                val total = root.totalSpace
                val usable = root.usableSpace
                val used = total - usable
                if (total == 0L) return@mapNotNull null

                val name = when {
                    path.toString() == "/" -> "Root (/)"
                    System.getProperty("os.name").lowercase().contains("win") ->
                        "Drive ${path.toString().take(2)}"
                    else -> path.toString()
                }

                val driveType = detectDriveType(path.toString())

                DriveInfo(
                    path = path.toString(),
                    name = name,
                    totalSpaceGb = total / 1_073_741_824.0,
                    usableSpaceGb = usable / 1_073_741_824.0,
                    usedSpaceGb = used / 1_073_741_824.0,
                    usagePercent = if (total > 0) (used.toFloat() / total.toFloat()) * 100f else 0f,
                    type = driveType
                )
            } catch (_: Exception) {
                null
            }
        }
        return StorageInfo(drives = drives)
    }

    private fun detectDriveType(path: String): DriveType {
        val os = System.getProperty("os.name").lowercase()
        return when {
            os.contains("win") -> when {
                path.startsWith("C:") -> DriveType.SYSTEM
                else -> DriveType.REMOVABLE
            }
            path == "/" -> DriveType.SYSTEM
            path.contains("media") || path.contains("mnt") -> DriveType.REMOVABLE
            path.contains("net") || path.contains("smb") -> DriveType.NETWORK
            else -> DriveType.UNKNOWN
        }
    }

    override fun observeStorageInfo(): Flow<StorageInfo> = flow {
        while (true) {
            emit(collectStorageInfo())
            delay(5000) // Storage changes less frequently
        }
    }

    override suspend fun getStorageInfo(): StorageInfo = collectStorageInfo()

    override suspend fun getDriveInfo(path: String): DriveInfo? {
        return collectStorageInfo().drives.find { it.path == path }
    }

    override suspend fun cleanTempFiles(): Long {
        var freedBytes = 0L
        val tempDirs = listOfNotNull(
            System.getProperty("java.io.tmpdir"),
            if (System.getProperty("os.name").lowercase().contains("win"))
                System.getenv("TEMP") else null,
            if (System.getProperty("os.name").lowercase().contains("win"))
                System.getenv("TMP") else null
        ).distinct()

        for (tempDir in tempDirs) {
            val dir = File(tempDir)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    try {
                        if (file.lastModified() < System.currentTimeMillis() - 86_400_000) {
                            val size = file.length()
                            if (file.delete()) {
                                freedBytes += size
                            }
                        }
                    } catch (_: Exception) { /* skip protected files */ }
                }
            }
        }
        return freedBytes
    }
}