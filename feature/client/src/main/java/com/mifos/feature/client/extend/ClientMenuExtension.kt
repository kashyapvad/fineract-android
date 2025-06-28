/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.client.extend

import androidx.compose.runtime.Composable

/**
 * Extension interface for adding custom menu items to client details
 * This allows minimal upstream changes while enabling custom functionality
 */
interface ClientMenuExtension {
    @Composable
    fun MenuItems(
        clientId: Int,
        onMenuClick: (route: String, clientId: Int) -> Unit,
    )
}

/**
 * Composable function that renders all registered client menu extensions
 * This is the only integration point needed in upstream code
 */
@Composable
fun ClientMenuExtensions(
    clientId: Int,
    extensions: Set<ClientMenuExtension>,
    onMenuClick: (route: String, clientId: Int) -> Unit,
) {
    extensions.forEach { extension ->
        extension.MenuItems(clientId = clientId, onMenuClick = onMenuClick)
    }
} 
