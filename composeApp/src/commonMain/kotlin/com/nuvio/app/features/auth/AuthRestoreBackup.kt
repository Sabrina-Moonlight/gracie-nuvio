package com.nuvio.app.features.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.features.settings.BackupRestartDialog
import com.nuvio.app.features.settings.SettingsBackup
import com.nuvio.app.features.settings.SettingsBackupResult
import com.nuvio.app.features.settings.rememberSettingsBackupFileActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_auth_restore_backup
import nuvio.composeapp.generated.resources.settings_advanced_backup_import_failed
import org.jetbrains.compose.resources.stringResource

/**
 * Botón de la pantalla de inicio para restaurar un backup local sin crear cuenta ni perfil.
 * Al terminar muestra el aviso de reinicio.
 */
@Composable
internal fun AuthRestoreBackupSection(
    enabled: Boolean,
    height: Dp,
    scale: Float,
) {
    val scope = rememberCoroutineScope()
    var failed by remember { mutableStateOf(false) }
    var showRestart by remember { mutableStateOf(false) }

    val fileActions = rememberSettingsBackupFileActions(
        onExportResult = {},
        onImportResult = { json ->
            // json == null: la persona canceló el selector, no se muestra error.
            if (json != null) {
                scope.launch {
                    val result = withContext(Dispatchers.Default) {
                        SettingsBackup.importFromJson(json)
                    }
                    if (result is SettingsBackupResult.Success) {
                        failed = false
                        showRestart = true
                    } else {
                        failed = true
                    }
                }
            }
        },
    )

    Spacer(modifier = Modifier.height(14.dp * scale))

    AuthSecondaryButton(
        text = stringResource(Res.string.compose_auth_restore_backup),
        enabled = enabled && !showRestart,
        height = height,
        scale = scale,
        onClick = { fileActions.importJson() },
    )

    if (failed) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(Res.string.settings_advanced_backup_import_failed),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.error,
                fontSize = (13f * scale).sp,
                lineHeight = (18f * scale).sp,
            ),
            textAlign = TextAlign.Center,
        )
    }

    BackupRestartDialog(isVisible = showRestart)
}
