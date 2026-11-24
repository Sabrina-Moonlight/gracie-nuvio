package com.nuvio.app.features.settings

import androidx.compose.runtime.Composable
import com.nuvio.app.core.ui.NuvioStatusModal
import com.nuvio.app.core.ui.rememberRestartApp
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.settings_backup_restart_confirm
import nuvio.composeapp.generated.resources.settings_backup_restart_message
import nuvio.composeapp.generated.resources.settings_backup_restart_title
import org.jetbrains.compose.resources.stringResource

/** Aviso que aparece tras restaurar un backup: reinicia la app para aplicar los cambios. */
@Composable
internal fun BackupRestartDialog(isVisible: Boolean) {
    val restartApp = rememberRestartApp()
    NuvioStatusModal(
        title = stringResource(Res.string.settings_backup_restart_title),
        message = stringResource(Res.string.settings_backup_restart_message),
        isVisible = isVisible,
        confirmText = stringResource(Res.string.settings_backup_restart_confirm),
        onConfirm = restartApp,
    )
}
