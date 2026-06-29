package com.p2ptaskmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.p2ptaskmanager.data.repository.PrefsRepository
import com.p2ptaskmanager.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val displayName: String = "",
    val peerId: String = "",
    val mqttBroker: String = "tcp://broker.hivemq.com:1883",
    val themeMode: String = "system",
    val exportedJson: String? = null,
    val isLoading: Boolean = true
)

class ProfileViewModel(
    private val prefs: PrefsRepository,
    private val taskRepo: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val name = prefs.getDisplayName()
            val peerId = prefs.getPeerId()
            val broker = prefs.getMqttBroker()
            val theme = prefs.getThemeMode()
            _uiState.value = ProfileUiState(
                displayName = name,
                peerId = peerId,
                mqttBroker = broker,
                themeMode = theme,
                isLoading = false
            )
        }
    }

    fun setDisplayName(name: String) {
        viewModelScope.launch {
            prefs.setDisplayName(name)
            _uiState.value = _uiState.value.copy(displayName = name)
        }
    }

    fun setMqttBroker(broker: String) {
        viewModelScope.launch {
            prefs.setMqttBroker(broker)
            _uiState.value = _uiState.value.copy(mqttBroker = broker)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            prefs.setThemeMode(mode)
            _uiState.value = _uiState.value.copy(themeMode = mode)
        }
    }

    fun exportData() {
        viewModelScope.launch {
            val json = taskRepo.exportToJson()
            _uiState.value = _uiState.value.copy(exportedJson = json)
        }
    }

    fun clearExport() {
        _uiState.value = _uiState.value.copy(exportedJson = null)
    }
}
