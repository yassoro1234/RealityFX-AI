package com.realityfx.ai.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.sin

/** Procedurally drawn sample scene, so the app ships with no image assets. */
object SampleImage {

    fun create(size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val s = size.toFloat()

        val sky = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, s * 0.75f,
                intArrayOf(0xFF1B2F4B.toInt(), 0xFFE9824A.toInt(), 0xFFFFD9A0.toInt()),
                floatArrayOf(0f, 0.8f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        c.drawRect(0f, 0f, s, s, sky)

        c.drawCircle(s * 0.72f, s * 0.38f, s * 0.11f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFF4D6.toInt() })

        drawRange(c, s, 0.52f, 0.10f, 0xFF4A5C78.toInt(), 0.5f)
        drawRange(c, s, 0.64f, 0.14f, 0xFF253447.toInt(), 2.1f)

        val road = Path().apply {
            moveTo(s * 0.42f, s * 0.70f)
            lineTo(s * 0.58f, s * 0.70f)
            lineTo(s * 0.95f, s)
            lineTo(s * 0.05f, s)
            close()
        }
        c.drawPath(road, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1A1F26.toInt() })

        val dash = Paint().apply { color = 0xFFEFEFEF.toInt() }
        for (i in 0 until 6) {
            val y0 = s * (0.74f + i * 0.045f)
            val half = s * (0.004f + i * 0.004f)
            c.drawRect(s * 0.5f - half, y0, s * 0.5f + half, y0 + s * 0.02f, dash)
        }

        val roof = RectF(s * 0.44f, s * 0.75f, s * 0.56f, s * 0.81f)
        c.drawRoundRect(roof, s * 0.02f, s * 0.02f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF0F172A.toInt() })
        val body = RectF(s * 0.40f, s * 0.80f, s * 0.60f, s * 0.88f)
        c.drawRoundRect(body, s * 0.015f, s * 0.015f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF22D3EE.toInt() })
        val wheel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF050608.toInt() }
        c.drawCircle(s * 0.43f, s * 0.88f, s * 0.014f, wheel)
        c.drawCircle(s * 0.57f, s * 0.88f, s * 0.014f, wheel)

        return bmp
    }

    private fun drawRange(c: Canvas, s: Float, baseY: Float, amp: Float, fill: Int, phase: Float) {
        val path = Path()
        path.moveTo(0f, s)
        val step = s / 64f
        var x = 0f
        while (x <= s) {
            val t = x / s
            val y = s * baseY - s * amp * (0.6f * sin(t * 6.3f + phase) + 0.4f * sin(t * 13.1f + phase * 2f))
            path.lineTo(x, y)
            x += step
        }
        path.lineTo(s, s)
        path.close()
        c.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fill })
    }
}
