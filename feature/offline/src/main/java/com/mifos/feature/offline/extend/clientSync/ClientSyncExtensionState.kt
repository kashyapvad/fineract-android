/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.offline.extend.clientSync

/**
 * Extension state for individual client sync tracking
 * Keeps extension logic separate from upstream state
 */
data class ClientSyncExtensionState(
    val syncStates: Map<Int, ClientSyncState> = emptyMap(),
    val isBulkSyncing: Boolean = false,
    val isIndividualSyncInProgress: Boolean = false
)

/**
 * Represents the sync state of an individual client
 */
data class ClientSyncState(
    val payloadId: Int,
    val status: SyncStatus,
    val progress: String = "",
    val error: String? = null
)

/**
 * Status of individual client sync operations
 */
enum class SyncStatus { 
    PENDING,      // Ready to sync
    SYNCING,      // Client sync in progress  
    KYC_SYNCING,  // KYC sync in progress
    SUCCESS,      // Successfully synced
    FAILED        // Sync failed
} 