package com.trishit.optimite.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector
import com.trishit.optimite.domain.model.MemoryInfo
import com.trishit.optimite.domain.model.OptimizationResult
import com.trishit.optimite.domain.model.ProcessInfo
import com.trishit.optimite.domain.model.StorageInfo
import com.trishit.optimite.domain.usecase.CleanStorageUseCase
import com.trishit.optimite.domain.usecase.GetTopProcessesUseCase
import com.trishit.optimite.domain.usecase.ObserveSystemStatsUseCase
import com.trishit.optimite.domain.usecase.OptimizeMemoryUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class ProcessSort { NAME, MEMORY, CPU, PID }

data class AppUiState(
    val memoryInfo: MemoryInfo? = null,
    val storageInfo: StorageInfo? = null,
    val processes: List<ProcessInfo> = emptyList(),
    val processSearchQuery: String = "",
    val processSortBy: ProcessSort = ProcessSort.MEMORY,
    val isOptimizing: Boolean = false,
    val isCleaning: Boolean = false,
    val lastOptimization: OptimizationResult? = null,
    val memoryHistory: List<Float> = emptyList(),
    val selectedTab: AppTab = AppTab.DASHBOARD,
    val error: String? = null
) {
    val filteredProcesses: List<ProcessInfo>
        get() = processes
            .filter { 
                it.name.contains(processSearchQuery, ignoreCase = true) || 
                it.pid.toString().contains(processSearchQuery) 
            }
            .let { list ->
                when (processSortBy) {
                    ProcessSort.NAME -> list.sortedBy { it.name.lowercase() }
                    ProcessSort.MEMORY -> list.sortedByDescending { it.memoryMb }
                    ProcessSort.CPU -> list.sortedByDescending { it.cpuPercent }
                    ProcessSort.PID -> list.sortedBy { it.pid }
                }
            }
}

enum class AppTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Rounded.Dashboard),
    MEMORY("Memory", Icons.Rounded.Memory),
    STORAGE("Storage", Icons.Rounded.Storage),
    PROCESSES("Processes", Icons.Rounded.TaskAlt)
}

class AppViewModel(
    private val observeSystem: ObserveSystemStatsUseCase,
    private val optimizeMemory: OptimizeMemoryUseCase,
    private val getProcesses: GetTopProcessesUseCase,
    private val cleanStorage: CleanStorageUseCase
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        observeSystemStats()
        observeProcesses()
    }

    private fun observeSystemStats() {
        scope.launch {
            observeSystem().collect { snapshot ->
                _state.update { current ->
                    val history = (current.memoryHistory + snapshot.memory.usagePercent)
                        .takeLast(60)
                    current.copy(
                        memoryInfo = snapshot.memory,
                        storageInfo = snapshot.storage,
                        memoryHistory = history,
                        error = null
                    )
                }
            }
        }
    }

    private fun observeProcesses() {
        scope.launch {
            // Increased limit to 50 for better filtering
            getProcesses.observe(50).collect { procs ->
                _state.update { it.copy(processes = procs) }
            }
        }
    }

    fun onProcessSearch(query: String) {
        _state.update { it.copy(processSearchQuery = query) }
    }

    fun onProcessSort(sort: ProcessSort) {
        _state.update { it.copy(processSortBy = sort) }
    }

    fun optimize() {
        if (_state.value.isOptimizing) return
        scope.launch {
            _state.update { it.copy(isOptimizing = true) }
            try {
                val result = optimizeMemory()
                _state.update { it.copy(lastOptimization = result, isOptimizing = false) }
                delay(4000)
                _state.update { it.copy(lastOptimization = null) }
            } catch (e: Exception) {
                _state.update { it.copy(isOptimizing = false, error = e.message) }
            }
        }
    }

    fun cleanTempFiles() {
        if (_state.value.isCleaning) return
        scope.launch {
            _state.update { it.copy(isCleaning = true) }
            try {
                val freed = cleanStorage()
                val mb = freed / 1_048_576
                val result = OptimizationResult(
                    freedMemoryMb = mb,
                    gcRunCount = 0,
                    message = "Cleaned temp files. Freed ~$mb MB"
                )
                _state.update { it.copy(isCleaning = false, lastOptimization = result) }
                delay(4000)
                _state.update { it.copy(lastOptimization = null) }
            } catch (e: Exception) {
                _state.update { it.copy(isCleaning = false, error = e.message) }
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun dispose() {
        scope.cancel()
    }
}
