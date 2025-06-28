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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mifos.core.objects.client.ClientPayload
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extension service that coordinates between main screen and extension architecture.
 * Keeps all extension logic contained in the extend folder.
 */
@Singleton
class SyncClientExtensionService @Inject constructor() {
    
    // Extension screen state - managed by service instead of main screen
    var showExtensionScreen by mutableStateOf(false)
        private set
    var individualSyncClientId by mutableStateOf<Int?>(null)
        private set
    var currentPayloads by mutableStateOf<List<ClientPayload>>(emptyList())
        private set
    
    /**
     * Updates current payloads being managed
     */
    fun updatePayloads(payloads: List<ClientPayload>) {
        currentPayloads = payloads
    }
    
    /**
     * Handles main sync button click - delegates to extension for validation
     */
    fun handleMainSyncClick(
        onShowValidation: () -> Unit,
        onDirectSync: () -> Unit
    ) {
        if (ClientSyncExtensionInterface.needsValidation(currentPayloads)) {
            individualSyncClientId = null // Clear individual sync for bulk
            showExtensionScreen = true
            onShowValidation()
        } else {
            onDirectSync()
        }
    }
    
    /**
     * Handles individual client sync click - delegates to extension for validation
     */
    fun handleIndividualSyncClick(
        payload: ClientPayload,
        onShowValidation: () -> Unit,
        onDirectSync: (ClientPayload) -> Unit
    ) {
        if (ClientSyncExtensionInterface.needsValidation(listOf(payload))) {
            individualSyncClientId = payload.id
            showExtensionScreen = true
            onShowValidation()
        } else {
            onDirectSync(payload)
        }
    }
    
    /**
     * Gets payloads for validation based on current context
     */
    fun getPayloadsForValidation(): List<ClientPayload> {
        return if (individualSyncClientId != null) {
            // Filter to specific client for individual sync
            currentPayloads.filter { it.id == individualSyncClientId }
        } else {
            // Bulk sync - validate all invalid payloads
            ClientSyncExtensionInterface.getInvalidPayloads(currentPayloads)
        }
    }
    
    /**
     * Handles validation completion
     */
    fun handleValidationComplete(onRefresh: () -> Unit) {
        showExtensionScreen = false
        individualSyncClientId = null
        onRefresh()
    }
    
    /**
     * Handles validation back button
     */
    fun handleValidationBack() {
        showExtensionScreen = false
        individualSyncClientId = null
    }
    
    /**
     * Extension validation screen wrapper - only shown when needed
     */
    @Composable
    fun ExtensionValidationScreenWrapper(
        onRefresh: () -> Unit,
        extensionViewModel: ClientSyncExtensionViewModel = hiltViewModel()
    ) {
        if (showExtensionScreen) {
            ClientSyncValidationWrapper(
                invalidPayloads = getPayloadsForValidation(),
                clientTemplate = null, // Will be loaded by extension ViewModel
                onBackPressed = { 
                    handleValidationBack()
                },
                onSyncComplete = { _ ->
                    handleValidationComplete(onRefresh)
                },
                onLoadingStateChange = { _ ->
                    // Extension manages its own loading state
                },
                extensionViewModel = extensionViewModel
            )
        }
    }
} 