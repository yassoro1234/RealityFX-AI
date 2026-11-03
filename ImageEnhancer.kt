package com.realityfx.ai.processing

import android.graphics.Bitmap
import com.realityfx.ai.model.Enhancement
import com.realityfx.ai.model.ProcessingMode

/**
 * On-device image enhancement for images rendered inside RealityFX AI.
 * Pure CPU pixel processing on a bounded-size bitmap. It does not, and cannot,
 * alter the graphics of other apps.
 */
object ImageEnhancer {

    fun enhance(source: Bitmap, settings: Enhancement, mode: ProcessingMode): Bitmap {
        val w = source.width
        val h = source.height
        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)

        var result = applyColor(pixels, settings)
        result = sharpen(result, w, h, settings.sharpness * 1.6f)
        if (mode == ProcessingMode.QUALITY && settings.glow > 0f) {
            result = addGlow(result, w, h, settings.glow)
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(result, 0, w, 0, 0, w, h)
        return out
    }

    /** Saturation, preset gains, contrast and brightness, applied per pixel. */
    private fun applyColor(src: IntArray, s: Enhancement): IntArray {
        val preset = s.preset
        val sat = s.saturation * preset.satMul
        val contrast = s.contrast
        val bright = s.brightness * 255f
        val out = IntArray(src.size)
        for (i in src.indices) {
            val c = src[i]
            val r0 = ((c shr 16) and 0xFF).toFloat()
            val g0 = ((c shr 8) and 0xFF).toFloat()
            val b0 = (c and 0xFF).toFloat()

            val lum = 0.2126f * r0 + 0.7152f * g0 + 0.0722f * b0
            var r = lum + sat * (r0 - lum)
            var g = lum + sat * (g0 - lum)
            var b = lum + sat * (b0 - lum)

            r *= preset.redGain
            b *= preset.blueGain

            r = (r - 128f) * contrast + 128f + bright
            g = (g - 128f) * contrast + 128f + bright
            b = (b - 128f) * contrast + 128f + bright

            out[i] = pack(r, g, b)
        }
        return out
    }

    /** Unsharp mask: original + amount * (original - blurred). */
    private fun sharpen(src: IntArray, w: Int, h: Int, amount: Float): IntArray {
        if (amount <= 0f) return src
        val blurred = boxBlur(src, w, h, 1)
        val out = IntArray(src.size)
        for (i in src.indices) {
            val o = src[i]
            val bl = blurred[i]
            out[i] = pack(
                sharpChannel((o shr 16) and 0xFF, (bl shr 16) and 0xFF, amount),
                sharpChannel((o shr 8) and 0xFF, (bl shr 8) and 0xFF, amount),
                sharpChannel(o and 0xFF, bl and 0xFF, amount)
            )
        }
        return out
    }

    private fun sharpChannel(orig: Int, blur: Int, amount: Float): Float =
        orig + amount * (orig - blur)

    /** Soft bloom from bright areas, blended back over the image. */
    private fun addGlow(src: IntArray, w: Int, h: Int, glow: Float): IntArray {
        val highlights = IntArray(src.size)
        for (i in src.indices) {
            val c = src[i]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            val lum = 0.2126f * r + 0.7152f * g + 0.0722f * b
            val weight = ((lum - 170f) / 85f).coerceIn(0f, 1f)
            highlights[i] = pack(r * weight, g * weight, b * weight)
        }
        val blurred = boxBlur(boxBlur(highlights, w, h, 4), w, h, 4)
        val k = glow * 0.9f
        val out = IntArray(src.size)
        for (i in src.indices) {
            val o = src[i]
            val bl = blurred[i]
            out[i] = pack(
                ((o shr 16) and 0xFF) + k * ((bl shr 16) and 0xFF),
                ((o shr 8) and 0xFF) + k * ((bl shr 8) and 0xFF),
                (o and 0xFF) + k * (bl and 0xFF)
            )
        }
        return out
    }

    /** Separable box blur with edge clamping. */
    private fun boxBlur(src: IntArray, w: Int, h: Int, radius: Int): IntArray {
        val div = 2 * radius + 1
        val tmp = IntArray(src.size)
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                var sr = 0
                var sg = 0
                var sb = 0
                for (k in -radius..radius) {
                    val c = src[row + (x + k).coerceIn(0, w - 1)]
                    sr += (c shr 16) and 0xFF
                    sg += (c shr 8) and 0xFF
                    sb += c and 0xFF
                }
                tmp[row + x] = (0xFF shl 24) or ((sr / div) shl 16) or ((sg / div) shl 8) or (sb / div)
            }
        }
        val out = IntArray(src.size)
        for (y in 0 until h) {
            for (x in 0 until w) {
                var sr = 0
                var sg = 0
                var sb = 0
                for (k in -radius..radius) {
                    val c = tmp[(y + k).coerceIn(0, h - 1) * w + x]
                    sr += (c shr 16) and 0xFF
                    sg += (c shr 8) and 0xFF
                    sb += c and 0xFF
                }
                out[y * w + x] = (0xFF shl 24) or ((sr / div) shl 16) or ((sg / div) shl 8) or (sb / div)
            }
        }
        return out
    }

    private fun pack(r: Float, g: Float, b: Float): Int =
        (0xFF shl 24) or (clamp(r) shl 16) or (clamp(g) shl 8) or clamp(b)

    private fun clamp(v: Float): Int = when {
        v <= 0f -> 0
        v >= 255f -> 255
        else -> v.toInt()
    }
}
