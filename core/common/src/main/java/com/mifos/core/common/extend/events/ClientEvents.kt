/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.common.extend.events

/**
 * Event-driven system for client operations to minimize upstream changes.
 * This allows extensions to listen for core events without modifying upstream ViewModels.
 */
data class ClientCreatedEvent(
    val serverClientId: Int,
    val localClientId: Int? = null,
    val source: String = "unknown"
)

/**
 * Event fired when KYC sync is completed for a client
 */
data class ClientSyncCompletedEvent(
    val serverClientId: Int,
    val localClientId: Int? = null,
    val success: Boolean = true,
    val error: String? = null,
    val source: String = "unknown"
)

/**
 * Simple event bus for client-related events.
 * Using a lightweight approach to avoid heavy dependencies.
 */
object ClientEventBus {
    private val listeners = mutableListOf<ClientEventListener>()
    
    fun subscribe(listener: ClientEventListener) {
        listeners.add(listener)
    }
    
    fun unsubscribe(listener: ClientEventListener) {
        listeners.remove(listener)
    }
    
    fun post(event: ClientCreatedEvent) {
        listeners.forEach { listener ->
            listener.onClientCreated(event)
        }
    }
    
    fun post(event: ClientSyncCompletedEvent) {
        listeners.forEach { listener ->
            listener.onClientSyncCompleted(event)
        }
    }
}

interface ClientEventListener {
    fun onClientCreated(event: ClientCreatedEvent)
    fun onClientSyncCompleted(event: ClientSyncCompletedEvent) {} // Default empty implementation
} 