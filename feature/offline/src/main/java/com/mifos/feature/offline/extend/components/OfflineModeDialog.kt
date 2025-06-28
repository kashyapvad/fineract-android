/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.offline.extend.components

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mifos.feature.offline.R

object OfflineModeDialog {
    
    /**
     * Shows a simple toast message for offline mode
     */
    fun showOfflineToast(context: Context) {
        Toast.makeText(
            context,
            context.getString(R.string.feature_offline_offline_sync_message),
            Toast.LENGTH_LONG
        ).show()
    }
    
    /**
     * Composable dialog for offline mode alert
     */
    @Composable
    fun OfflineModeAlertDialog(
        showDialog: Boolean,
        onDismiss: () -> Unit,
        title: String = stringResource(R.string.feature_offline_offline_mode),
        message: String = stringResource(R.string.feature_offline_offline_sync_alert)
    ) {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(text = title)
                },
                text = {
                    Text(text = message)
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.feature_offline_ok))
                    }
                }
            )
        }
    }
    
    /**
     * Shows offline mode handling with toast and optional callback
     */
    fun handleOfflineMode(
        context: Context, 
        onOfflineDetected: (() -> Unit)? = null
    ) {
        showOfflineToast(context)
        onOfflineDetected?.invoke()
    }
} 