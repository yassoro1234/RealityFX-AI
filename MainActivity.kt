package com.realityfx.ai

import android.content.Context
import android.os.Bundle
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.realityfx.ai.data.AppPrefs
import com.realityfx.ai.ui.RealityFxApp
import com.realityfx.ai.ui.RealityFxTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var prefs: AppPrefs
    private val amoled = mutableStateOf(false)

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = AppPrefs(this)
        amoled.value = prefs.amoled

        setContent {
            RealityFxTheme(amoled = amoled.value) {
                RealityFxApp(
                    prefs = prefs,
                    amoled = amoled,
                    onLanguageChanged = { recreate() }
                )
            }
        }
    }
}

/** Applies the saved English or Arabic language before the activity is created. */
object LanguageHelper {
    fun wrap(base: Context): Context {
        val locale = Locale.forLanguageTag(AppPrefs(base).language)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
