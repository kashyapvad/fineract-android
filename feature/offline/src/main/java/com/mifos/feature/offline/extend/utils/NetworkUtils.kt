/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.offline.extend.utils

import android.content.Context
import android.widget.Toast
import com.mifos.core.common.utils.Network
import com.mifos.feature.offline.R

/**
 * Generic network check utility that eliminates duplication between
 * different sync operation network checks.
 */
object NetworkUtils {
    
    /**
     * Checks network connection and executes operation if online,
     * shows error toast if offline.
     */
    fun checkNetworkAndExecute(
        context: Context,
        operation: () -> Unit
    ) {
        if (Network.isOnline(context)) {
            operation()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.feature_offline_error_not_connected_internet),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
} 