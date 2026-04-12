package com.trishit.optimite.ui

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

data class AppUiState(
    val memoryInfo: MemoryInfo? = null,
    val storageInfo: StorageInfo? = null,
    val processes: List<ProcessInfo> = emptyList(),
    val isOptimizing: Boolean = false,
    val isCleaning: Boolean = false,
    val lastOptimization: OptimizationResult? = null,
    val memoryHistory: List<Float> = emptyList(),
    val selectedTab: AppTab = AppTab.DASHBOARD,
    val error: String? = null
)

enum class AppTab(val label: String, val icon: String) {
    DASHBOARD("Dashboard", "⬡"),
    MEMORY("Memory", "◈"),
    STORAGE("Storage", "▣"),
    PROCESSES("Processes", "⊞")
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
            getProcesses.observe(15).collect { procs ->
                _state.update { it.copy(processes = procs) }
            }
        }
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