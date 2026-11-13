package com.nuvio.app.features.settings

import androidx.compose.runtime.Composable

/**
 * Platform file picker for settings backup export / import.
 *
 * Android uses Storage Access Framework (CreateDocument / OpenDocument).
 */
internal data class SettingsBackupFileActions(
    val exportJson: (json: String, suggestedFileName: String) -> Unit,
    val importJson: () -> Unit,
)

@Composable
internal expect fun rememberSettingsBackupFileActions(
    onExportResult: (success: Boolean) -> Unit,
    onImportResult: (json: String?) -> Unit,
): SettingsBackupFileActions
