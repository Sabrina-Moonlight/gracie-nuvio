package com.nuvio.app.core.ui

import androidx.compose.runtime.Composable

/** Devuelve una función que cierra la app y la vuelve a abrir. */
@Composable
expect fun rememberRestartApp(): () -> Unit
