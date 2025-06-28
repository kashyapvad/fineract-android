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

import com.mifos.core.objects.client.ClientPayload
import kotlinx.coroutines.flow.StateFlow

/**
 * Bridge interface for extension state management.
 * Allows extensions to provide state without polluting upstream ViewModel.
 */
interface SyncExtensionBridge {
    
    /**
     * Provides extension-specific state to be included in upstream UI state
     */
    val extensionState: StateFlow<Any?>
    
    /**
     * Handles individual client sync operations
     */
    fun syncIndividualClient(payload: ClientPayload)
    
    /**
     * Validates client payload before sync
     */
    fun validateClientPayload(payload: ClientPayload): List<String>
    
    /**
     * Checks if individual sync is currently in progress
     */
    fun isIndividualSyncInProgress(): Boolean
    
    /**
     * Checks if bulk sync should be blocked by extension operations
     */
    fun shouldBlockBulkSync(): Boolean
} 