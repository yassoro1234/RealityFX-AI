package com.realityfx.ai.model

/** Color grading presets. Values are simple per-channel gains applied on-device. */
enum class FilterPreset(val satMul: Float, val redGain: Float, val blueGain: Float) {
    REALISTIC(1.00f, 1.00f, 1.00f),
    CINEMATIC(0.95f, 1.06f, 0.93f),
    VIVID(1.35f, 1.00f, 1.00f),
    COOL(0.95f, 0.92f, 1.08f);

    companion object {
        fun fromName(name: String?): FilterPreset =
            entries.firstOrNull { it.name == name } ?: REALISTIC
    }
}

enum class ProcessingMode { BATTERY_SAVER, QUALITY }

/** All user-adjustable image settings. */
data class Enhancement(
    val sharpness: Float = 0.35f,  // 0.0 .. 1.0
    val contrast: Float = 1.0f,    // 0.5 .. 1.5
    val saturation: Float = 1.0f,  // 0.0 .. 2.0
    val brightness: Float = 0.0f,  // -0.3 .. 0.3
    val glow: Float = 0.0f,        // 0.0 .. 1.0
    val preset: FilterPreset = FilterPreset.REALISTIC
) {
    companion object {
        val DEFAULT = Enhancement()
    }
}

data class GameProfile(
    val id: String,
    val name: String,
    val settings: Enhancement
)
