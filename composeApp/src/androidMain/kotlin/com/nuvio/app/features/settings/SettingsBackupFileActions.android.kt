package com.nuvio.app.features.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

@Composable
internal actual fun rememberSettingsBackupFileActions(
    onExportResult: (success: Boolean) -> Unit,
    onImportResult: (json: String?) -> Unit,
): SettingsBackupFileActions {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Holds the JSON to write when the CreateDocument URI comes back.
    val pendingExportJson = remember { arrayOfNulls<String>(1) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri: Uri? ->
        if (uri == null) {
            onExportResult(false)
            return@rememberLauncherForActivityResult
        }
        val json = pendingExportJson[0]
        pendingExportJson[0] = null
        if (json == null) {
            onExportResult(false)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(json.toByteArray(StandardCharsets.UTF_8))
                        output.flush()
                    } ?: error("null output stream")
                    true
                }.getOrDefault(false)
            }
            onExportResult(ok)
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) {
            onImportResult(null)
            return@rememberLauncherForActivityResult
        }
        // Persist read permission for the session when possible.
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        scope.launch {
            val json = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        input.readBytes().toString(StandardCharsets.UTF_8)
                    }
                }.getOrNull()
            }
            onImportResult(json)
        }
    }

    return remember(createDocumentLauncher, openDocumentLauncher) {
        SettingsBackupFileActions(
            exportJson = { json, suggestedFileName ->
                pendingExportJson[0] = json
                runCatching {
                    createDocumentLauncher.launch(suggestedFileName)
                }.onFailure {
                    pendingExportJson[0] = null
                    onExportResult(false)
                }
            },
            importJson = {
                runCatching {
                    openDocumentLauncher.launch(arrayOf("application/json", "text/plain", "text/*", "*/*"))
                }.onFailure {
                    onImportResult(null)
                }
            },
        )
    }
}
