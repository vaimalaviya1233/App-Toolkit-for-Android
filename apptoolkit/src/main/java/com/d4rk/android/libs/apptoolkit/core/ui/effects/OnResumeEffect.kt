package com.d4rk.android.libs.apptoolkit.core.ui.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun OnResumeEffect(onResume : () -> Unit) {
    val lifecycleOwner : LifecycleOwner = LocalLifecycleOwner.current
    val latestOnResume : () -> Unit by rememberUpdatedState<() -> Unit>(newValue = onResume)

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _ : LifecycleOwner , event : Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_RESUME) latestOnResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer = observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer = observer) }
    }
}