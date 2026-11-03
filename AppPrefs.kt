package com.realityfx.ai.data

import android.content.Context
import android.content.SharedPreferences
import com.realityfx.ai.model.Enhancement
import com.realityfx.ai.model.FilterPreset
import com.realityfx.ai.model.GameProfile
import com.realityfx.ai.model.ProcessingMode
import org.json.JSONArray
import org.json.JSONObject

/** Local-only preferences. Nothing leaves the device. */
class AppPrefs(context: Context) {

    private val sp: SharedPreferences =
        context.getSharedPreferences("realityfx_prefs", Context.MODE_PRIVATE)

    var language: String
        get() = sp.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) {
            sp.edit().putString(KEY_LANGUAGE, value).apply()
        }

    var amoled: Boolean
        get() = sp.getBoolean(KEY_AMOLED, false)
        set(value) {
            sp.edit().putBoolean(KEY_AMOLED, value).apply()
        }

    var mode: ProcessingMode
        get() = if (sp.getString(KEY_MODE, "quality") == "battery") {
            ProcessingMode.BATTERY_SAVER
        } else {
            ProcessingMode.QUALITY
        }
        set(value) {
            val stored = if (value == ProcessingMode.BATTERY_SAVER) "battery" else "quality"
            sp.edit().putString(KEY_MODE, stored).apply()
        }

    var showFps: Boolean
        get() = sp.getBoolean(KEY_SHOW_FPS, false)
        set(value) {
            sp.edit().putBoolean(KEY_SHOW_FPS, value).apply()
        }

    var activeProfileId: String
        get() = sp.getString(KEY_ACTIVE_PROFILE, DEFAULT_ID) ?: DEFAULT_ID
        set(value) {
            sp.edit().putString(KEY_ACTIVE_PROFILE, value).apply()
        }

    fun loadProfiles(): List<GameProfile> {
        val list = mutableListOf<GameProfile>()
        val raw = sp.getString(KEY_PROFILES, null)
        if (raw != null) {
            try {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list += GameProfile(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        settings = Enhancement(
                            sharpness = o.getDouble("sharpness").toFloat(),
                            contrast = o.getDouble("contrast").toFloat(),
                            saturation = o.getDouble("saturation").toFloat(),
                            brightness = o.getDouble("brightness").toFloat(),
                            glow = o.getDouble("glow").toFloat(),
                            preset = FilterPreset.fromName(o.optString("preset"))
                        )
                    )
                }
            } catch (e: Exception) {
                // Corrupt data: fall back to defaults rather than crashing.
                list.clear()
            }
        }
        if (list.none { it.id == DEFAULT_ID }) {
            list.add(0, GameProfile(DEFAULT_ID, "Default", Enhancement.DEFAULT))
        }
        return list
    }

    fun saveProfiles(profiles: List<GameProfile>) {
        val arr = JSONArray()
        for (p in profiles) {
            arr.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("sharpness", p.settings.sharpness.toDouble())
                    .put("contrast", p.settings.contrast.toDouble())
                    .put("saturation", p.settings.saturation.toDouble())
                    .put("brightness", p.settings.brightness.toDouble())
                    .put("glow", p.settings.glow.toDouble())
                    .put("preset", p.settings.preset.name)
            )
        }
        sp.edit().putString(KEY_PROFILES, arr.toString()).apply()
    }

    companion object {
        const val DEFAULT_ID = "default"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_AMOLED = "amoled"
        private const val KEY_MODE = "mode"
        private const val KEY_SHOW_FPS = "show_fps"
        private const val KEY_ACTIVE_PROFILE = "active_profile"
        private const val KEY_PROFILES = "profiles"
    }
}
