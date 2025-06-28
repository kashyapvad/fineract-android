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

import com.mifos.core.common.extend.events.ClientEventBus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extension initializer that sets up the event-driven system.
 * This allows extensions to work with minimal changes to upstream code.
 */
@Singleton
class ExtensionInitializer @Inject constructor(
    private val kycSyncEventListener: KycSyncEventListener
) {
    
    private var initialized = false
    
    /**
     * Initialize all extension event listeners.
     * This should be called once during application startup.
     */
    fun initialize() {
        if (!initialized) {
            // Subscribe KYC sync listener to client events
            ClientEventBus.subscribe(kycSyncEventListener)
            initialized = true
        }
    }
    
    /**
     * Cleanup extension listeners (for testing or app shutdown)
     */
    fun cleanup() {
        ClientEventBus.unsubscribe(kycSyncEventListener)
        initialized = false
    }
} 