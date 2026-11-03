package com.realityfx.ai.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.realityfx.ai.R
import com.realityfx.ai.model.ProcessingMode

@Composable
fun SettingsScreen(
    language: String,
    amoled: Boolean,
    mode: ProcessingMode,
    showFps: Boolean,
    onBack: () -> Unit,
    onLanguage: (String) -> Unit,
    onAmoled: (Boolean) -> Unit,
    onMode: (ProcessingMode) -> Unit,
    onShowFps: (Boolean) -> Unit
) {
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
            OutlinedButton(onClick = onBack) { Text(stringResource(R.string.nav_back)) }
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Cyan
            )
        }

        GlassCard {
            SectionTitle(stringResource(R.string.section_language))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = language == "en",
                    onClick = { onLanguage("en") },
                    label = { Text(stringResource(R.string.lang_english)) }
                )
                FilterChip(
                    selected = language == "ar",
                    onClick = { onLanguage("ar") },
                    label = { Text(stringResource(R.string.lang_arabic)) }
                )
            }
        }

        GlassCard {
            SectionTitle(stringResource(R.string.section_theme))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !amoled,
                    onClick = { onAmoled(false) },
                    label = { Text(stringResource(R.string.theme_dark)) }
                )
                FilterChip(
                    selected = amoled,
                    onClick = { onAmoled(true) },
                    label = { Text(stringResource(R.string.theme_amoled)) }
                )
            }
        }

        GlassCard {
            SectionTitle(stringResource(R.string.section_performance))
            ModeOption(
                title = stringResource(R.string.mode_battery),
                description = stringResource(R.string.mode_battery_desc),
                selected = mode == ProcessingMode.BATTERY_SAVER,
                onClick = { onMode(ProcessingMode.BATTERY_SAVER) }
            )
            ModeOption(
                title = stringResource(R.string.mode_quality),
                description = stringResource(R.string.mode_quality_desc),
                selected = mode == ProcessingMode.QUALITY,
                onClick = { onMode(ProcessingMode.QUALITY) }
            )
            ToggleRow(
                label = stringResource(R.string.show_fps),
                checked = showFps,
                onChange = onShowFps
            )
        }

        GlassCard {
            SectionTitle(stringResource(R.string.section_privacy))
            Text(stringResource(R.string.privacy_summary), color = MutedText)
        }

        GlassCard {
            SectionTitle(stringResource(R.string.section_limits))
            Text(stringResource(R.string.limits_text), color = MutedText)
        }

        GlassCard {
            SectionTitle(stringResource(R.string.section_about))
            Text(stringResource(R.string.about_text), color = MutedText)
        }

    }
}

@Composable
private fun ModeOption(title: String, description: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = if (selected) Cyan else MaterialTheme.colorScheme.onBackground,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        Text(description, style = MaterialTheme.typography.bodySmall, color = MutedText)
    }
}
