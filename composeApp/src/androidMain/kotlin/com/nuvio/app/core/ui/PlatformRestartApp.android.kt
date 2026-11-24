package com.nuvio.app.core.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.system.exitProcess

private const val EXTRA_NEXT_INTENT = "restart_next_intent"

@Composable
actual fun rememberRestartApp(): () -> Unit {
    val context = LocalContext.current.applicationContext
    return remember(context) { { restartApp(context) } }
}

private fun restartApp(context: Context) {
    val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
    if (launch == null) {
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }
    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    // La pantalla auxiliar vive en otro proceso: así puede abrir la app de nuevo
    // aunque este proceso ya haya muerto.
    val helper = Intent(context, RestartHelperActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .putExtra(EXTRA_NEXT_INTENT, launch)
    context.startActivity(helper)
    Process.killProcess(Process.myPid())
    exitProcess(0)
}

/** Corre en el proceso ":restart" (ver AndroidManifest). Abre la app y se cierra. */
class RestartHelperActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        val next = intent.getParcelableExtra<Intent>(EXTRA_NEXT_INTENT)
        if (next != null) startActivity(next)
        finish()
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }
}
