package com.realityfx.ai.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.realityfx.ai.data.AppPrefs

@Composable
fun RealityFxApp(
    prefs: AppPrefs,
    amoled: MutableState<Boolean>,
    onLanguageChanged: () -> Unit
) {
    val vm: MainViewModel = viewModel()
    val mode by vm.mode.collectAsState()
    val showFps by vm.showFps.collectAsState()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var language by remember { mutableStateOf(prefs.language) }

    BackHandler(enabled = showSettings) { showSettings = false }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            if (showSettings) {
                SettingsScreen(
                    language = language,
                    amoled = amoled.value,
                    mode = mode,
                    showFps = showFps,
                    onBack = { showSettings = false },
                    onLanguage = { code ->
                        if (code != language) {
                            language = code
                            prefs.language = code
                            onLanguageChanged()
                        }
                    },
                    onAmoled = { enabled ->
                        amoled.value = enabled
                        prefs.amoled = enabled
                    },
                    onMode = { vm.setMode(it) },
                    onShowFps = { vm.setShowFps(it) }
                )
            } else {
                HomeScreen(
                    vm = vm,
                    onOpenSettings = { showSettings = true }
                )
            }
        }
    }
}
