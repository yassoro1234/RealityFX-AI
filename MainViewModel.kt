package com.realityfx.ai.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.realityfx.ai.R
import com.realityfx.ai.data.AppPrefs
import com.realityfx.ai.model.Enhancement
import com.realityfx.ai.model.GameProfile
import com.realityfx.ai.model.ProcessingMode
import com.realityfx.ai.processing.ImageEnhancer
import com.realityfx.ai.processing.SampleImage
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = AppPrefs(application)

    private val _profiles = MutableStateFlow(prefs.loadProfiles())
    val profiles: StateFlow<List<GameProfile>> = _profiles.asStateFlow()

    private val _activeId = MutableStateFlow(prefs.activeProfileId)
    val activeId: StateFlow<String> = _activeId.asStateFlow()

    private val _settings = MutableStateFlow(
        _profiles.value.firstOrNull { it.id == prefs.activeProfileId }?.settings
            ?: Enhancement.DEFAULT
    )
    val settings: StateFlow<Enhancement> = _settings.asStateFlow()

    private val _mode = MutableStateFlow(prefs.mode)
    val mode: StateFlow<ProcessingMode> = _mode.asStateFlow()

    private val _showFps = MutableStateFlow(prefs.showFps)
    val showFps: StateFlow<Boolean> = _showFps.asStateFlow()

    private val _source = MutableStateFlow<Bitmap?>(null)
    val source: StateFlow<Bitmap?> = _source.asStateFlow()

    private val _result = MutableStateFlow<Bitmap?>(null)
    val result: StateFlow<Bitmap?> = _result.asStateFlow()

    private var processJob: Job? = null

    init {
        regenerateSource()
    }

    private fun sampleSize(): Int =
        if (_mode.value == ProcessingMode.QUALITY) 512 else 320

    private fun debounceMs(): Long =
        if (_mode.value == ProcessingMode.QUALITY) 60L else 120L

    /** Rebuilds the sample image at the size for the current mode, then processes it. */
    private fun regenerateSource() {
        processJob?.cancel()
        processJob = viewModelScope.launch {
            val size = sampleSize()
            _source.value = withContext(Dispatchers.Default) { SampleImage.create(size) }
            runProcessing(0L)
        }
    }

    /** Debounced re-processing so slider drags do not flood the CPU. */
    private fun scheduleProcessing() {
        processJob?.cancel()
        processJob = viewModelScope.launch { runProcessing(debounceMs()) }
    }

    private suspend fun runProcessing(delayMs: Long) {
        if (delayMs > 0L) delay(delayMs)
        val src = _source.value ?: return
        val s = _settings.value
        val m = _mode.value
        _result.value = withContext(Dispatchers.Default) { ImageEnhancer.enhance(src, s, m) }
    }

    fun updateSettings(newSettings: Enhancement) {
        _settings.value = newSettings
        scheduleProcessing()
    }

    fun resetToDefaults() = updateSettings(Enhancement.DEFAULT)

    fun selectProfile(id: String) {
        val profile = _profiles.value.firstOrNull { it.id == id } ?: return
        prefs.activeProfileId = id
        _activeId.value = id
        _settings.value = profile.settings
        scheduleProcessing()
    }

    fun saveToActiveProfile() {
        val id = _activeId.value
        val updated = _profiles.value.map {
            if (it.id == id) it.copy(settings = _settings.value) else it
        }
        _profiles.value = updated
        prefs.saveProfiles(updated)
    }

    fun createProfile() {
        val name = getApplication<Application>()
            .getString(R.string.profile_new_name, _profiles.value.size + 1)
        val profile = GameProfile(UUID.randomUUID().toString(), name, _settings.value)
        val updated = _profiles.value + profile
        _profiles.value = updated
        prefs.saveProfiles(updated)
        selectProfile(profile.id)
    }

    fun deleteActiveProfile() {
        val id = _activeId.value
        if (id == AppPrefs.DEFAULT_ID) return
        val updated = _profiles.value.filterNot { it.id == id }
        _profiles.value = updated
        prefs.saveProfiles(updated)
        selectProfile(AppPrefs.DEFAULT_ID)
    }

    fun setMode(newMode: ProcessingMode) {
        if (newMode == _mode.value) return
        _mode.value = newMode
        prefs.mode = newMode
        regenerateSource()
    }

    fun setShowFps(enabled: Boolean) {
        _showFps.value = enabled
        prefs.showFps = enabled
    }
}
