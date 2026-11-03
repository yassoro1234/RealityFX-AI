package com.realityfx.ai.ui

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.realityfx.ai.R
import com.realityfx.ai.data.AppPrefs
import com.realityfx.ai.model.Enhancement
import com.realityfx.ai.model.FilterPreset
import com.realityfx.ai.model.ProcessingMode
import kotlin.math.roundToInt

@Composable
fun HomeScreen(vm: MainViewModel, onOpenSettings: () -> Unit) {
    val profiles by vm.profiles.collectAsState()
    val activeId by vm.activeId.collectAsState()
    val settings by vm.settings.collectAsState()
    val mode by vm.mode.collectAsState()
    val showFps by vm.showFps.collectAsState()
    val source by vm.source.collectAsState()
    val result by vm.result.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Cyan
                )
                Text(
                    text = stringResource(R.string.tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }
            OutlinedButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.nav_settings))
            }
        }

        ProfileCard(
            profiles = profiles,
            activeId = activeId,
            onSelect = { vm.selectProfile(it) },
            onSave = {
                vm.saveToActiveProfile()
                Toast.makeText(context, R.string.saved_toast, Toast.LENGTH_SHORT).show()
            },
            onNew = { vm.createProfile() },
            onDelete = { vm.deleteActiveProfile() }
        )

        PreviewCard(source = source, result = result, showFps = showFps)

        ControlsCard(
            settings = settings,
            mode = mode,
            onChange = { vm.updateSettings(it) }
        )

        OutlinedButton(
            onClick = { vm.resetToDefaults() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.reset))
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileCard(
    profiles: List<com.realityfx.ai.model.GameProfile>,
    activeId: String,
    onSelect: (String) -> Unit,
    onSave: () -> Unit,
    onNew: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard {
        SectionTitle(stringResource(R.string.section_profile))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            profiles.forEach { p ->
                val name = if (p.id == AppPrefs.DEFAULT_ID) {
                    stringResource(R.string.profile_default_name)
                } else {
                    p.name
                }
                FilterChip(
                    selected = p.id == activeId,
                    onClick = { onSelect(p.id) },
                    label = { Text(name) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onSave) { Text(stringResource(R.string.profile_save)) }
            OutlinedButton(onClick = onNew) { Text(stringResource(R.string.profile_new)) }
            TextButton(
                onClick = onDelete,
                enabled = activeId != AppPrefs.DEFAULT_ID
            ) {
                Text(stringResource(R.string.profile_delete))
            }
        }
    }
}

@Composable
private fun PreviewCard(source: Bitmap?, result: Bitmap?, showFps: Boolean) {
    var split by remember { mutableFloatStateOf(0.5f) }
    var widthPx by remember { mutableFloatStateOf(1f) }

    GlassCard {
        SectionTitle(stringResource(R.string.section_preview))
        Text(
            text = stringResource(R.string.preview_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MutedText
        )

        if (source != null && result != null) {
            val before = remember(source) { source.asImageBitmap() }
            val after = remember(result) { result.asImageBitmap() }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .onSizeChanged { widthPx = it.width.toFloat() }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            split = (change.position.x / widthPx).coerceIn(0f, 1f)
                        }
                    }
            ) {
                val dst = IntSize(size.width.roundToInt(), size.height.roundToInt())
                drawImage(before, dstSize = dst)
                clipRect(0f, 0f, size.width * split, size.height) {
                    drawImage(after, dstSize = dst)
                }
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * split, 0f),
                    end = Offset(size.width * split, size.height),
                    strokeWidth = 3.dp.toPx()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.preview_before), color = MutedText)
                Text(stringResource(R.string.preview_after), color = Cyan)
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Cyan)
            }
        }

        if (showFps) {
            FpsReadout()
        }
    }
}

/**
 * Frame rate of the RealityFX screen itself, measured from display frame callbacks.
 * It says nothing about other apps or games.
 */
@Composable
private fun FpsReadout() {
    var fps by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        var frames = 0
        var windowStart = -1L
        while (true) {
            withFrameNanos { now ->
                if (windowStart < 0L) windowStart = now
                frames++
                val elapsed = now - windowStart
                if (elapsed >= 1_000_000_000L) {
                    fps = frames * 1_000_000_000f / elapsed
                    frames = 0
                    windowStart = now
                }
            }
        }
    }

    Text(
        text = stringResource(R.string.fps_label),
        style = MaterialTheme.typography.bodySmall,
        color = MutedText
    )
    Text(
        text = if (fps <= 0f) stringResource(R.string.fps_measuring)
        else stringResource(R.string.fps_value, fps),
        style = MaterialTheme.typography.titleMedium,
        color = Cyan,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ControlsCard(
    settings: Enhancement,
    mode: ProcessingMode,
    onChange: (Enhancement) -> Unit
) {
    GlassCard {
        SectionTitle(stringResource(R.string.section_controls))

        SliderRow(
            label = stringResource(R.string.sharpness),
            value = settings.sharpness,
            range = 0f..1f,
            valueText = "${(settings.sharpness * 100).roundToInt()}%",
            onChange = { onChange(settings.copy(sharpness = it)) }
        )
        SliderRow(
            label = stringResource(R.string.contrast),
            value = settings.contrast,
            range = 0.5f..1.5f,
            valueText = "%.2f".format(settings.contrast),
            onChange = { onChange(settings.copy(contrast = it)) }
        )
        SliderRow(
            label = stringResource(R.string.saturation),
            value = settings.saturation,
            range = 0f..2f,
            valueText = "%.2f".format(settings.saturation),
            onChange = { onChange(settings.copy(saturation = it)) }
        )
        SliderRow(
            label = stringResource(R.string.brightness),
            value = settings.brightness,
            range = -0.3f..0.3f,
            valueText = "%+.2f".format(settings.brightness),
            onChange = { onChange(settings.copy(brightness = it)) }
        )

        val glowAllowed = mode == ProcessingMode.QUALITY
        SliderRow(
            label = stringResource(R.string.glow),
            value = settings.glow,
            range = 0f..1f,
            valueText = "${(settings.glow * 100).roundToInt()}%",
            enabled = glowAllowed,
            onChange = { onChange(settings.copy(glow = it)) }
        )
        if (!glowAllowed) {
            Text(
                text = stringResource(R.string.glow_battery_note),
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )
        }

        SectionTitle(stringResource(R.string.section_filter))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPreset.entries.forEach { preset ->
                FilterChip(
                    selected = settings.preset == preset,
                    onClick = { onChange(settings.copy(preset = preset)) },
                    label = { Text(stringResource(presetLabel(preset))) }
                )
            }
        }
    }
}

private fun presetLabel(preset: FilterPreset): Int = when (preset) {
    FilterPreset.REALISTIC -> R.string.filter_realistic
    FilterPreset.CINEMATIC -> R.string.filter_cinematic
    FilterPreset.VIVID -> R.string.filter_vivid
    FilterPreset.COOL -> R.string.filter_cool
}
