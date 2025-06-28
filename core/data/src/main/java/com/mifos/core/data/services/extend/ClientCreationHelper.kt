/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.data.services.extend

import com.mifos.core.common.extend.events.ClientCreatedEvent
import com.mifos.core.common.extend.events.ClientEventBus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Minimal helper for client creation events.
 * Upstream ViewModels can use this with a single line to trigger extensions.
 */
@Singleton
class ClientCreationHelper @Inject constructor(
    private val extensionInitializer: ExtensionInitializer
) {
    
    /**
     * Notify extensions that a client was created.
     * This is the only method upstream ViewModels need to call.
     */
    fun notifyClientCreated(serverClientId: Int, localClientId: Int? = null, source: String = "sync") {
        // Ensure extensions are initialized  
        try {
            extensionInitializer.initialize()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Post the event
        ClientEventBus.post(
            ClientCreatedEvent(
                serverClientId = serverClientId,
                localClientId = localClientId,
                source = source
            )
        )
    }
} 