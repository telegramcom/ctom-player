package com.ctom.player.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ctom.player.CtomPlayerApplication
import com.ctom.player.data.MediaLibrary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as CtomPlayerApplication).mediaRepository
    private val _library = MutableStateFlow(MediaLibrary())
    val library: StateFlow<MediaLibrary> = _library.asStateFlow()
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun refresh(hasPermission: Boolean) {
        if (!hasPermission || _isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            _error.value = null
            runCatching { repository.scan() }
                .onSuccess { _library.value = it }
                .onFailure { _error.value = "Media access could not be read. Check your Android permissions." }
            _isScanning.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}