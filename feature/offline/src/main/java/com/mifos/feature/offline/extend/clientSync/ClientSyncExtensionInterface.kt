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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mifos.core.objects.client.ClientPayload
import com.mifos.core.objects.templates.clients.ClientsTemplate

/**
 * Extension interface for client sync functionality.
 * This provides a clean integration point for upstream files.
 */
object ClientSyncExtensionInterface {
    /**
     * Checks if payloads need validation
     */
    fun needsValidation(payloads: List<ClientPayload>): Boolean {
        return ClientSyncValidationExtension.hasValidationIssues(payloads)
    }

    /**
     * Gets invalid payloads that need validation
     */
    fun getInvalidPayloads(payloads: List<ClientPayload>): List<ClientPayload> {
        return ClientSyncValidationExtension.getInvalidPayloads(payloads)
    }

    /**
     * Checks if KYC data is available for any client
     */
    fun isKycDataAvailable(payloads: List<ClientPayload>): Boolean {
        // Check if any client has KYC data that needs syncing
        return payloads.any { payload ->
            // KYC data is considered available if client has payload ID
            // This indicates a client that may have associated KYC data
            val payloadId = payload.id
            payloadId != null && payloadId > 0
        }
    }

    /**
     * Provides conditional KYC sync section composable
     * Only shows when KYC data is actually available
     */
    @Composable
    fun getKycSyncSection(
        payloads: List<ClientPayload>,
        modifier: Modifier = Modifier
    ) {
        // Only show KYC section if KYC data is available
        if (isKycDataAvailable(payloads)) {
            com.mifos.feature.offline.extend.kyc.SyncKycSection(modifier = modifier)
        }
        // If no KYC data, render nothing (empty composable)
    }
}

/**
 * Composable wrapper for the validation screen
 */
@Composable
fun ClientSyncValidationWrapper(
    invalidPayloads: List<ClientPayload>,
    clientTemplate: ClientsTemplate?,
    onBackPressed: () -> Unit,
    onSyncComplete: (List<ClientSyncResult>) -> Unit,
    onLoadingStateChange: ((Boolean) -> Unit)? = null,
    extensionViewModel: ClientSyncExtensionViewModel = hiltViewModel(),
) {
    val syncResults by extensionViewModel.syncResults.collectAsState()
    val loadedClientTemplate by extensionViewModel.clientTemplate.collectAsState()
    val isUpdateAndSyncInProgress by extensionViewModel.isUpdateAndSyncInProgress.collectAsState()

    // Communicate loading state changes to parent
    LaunchedEffect(isUpdateAndSyncInProgress) {
        onLoadingStateChange?.invoke(isUpdateAndSyncInProgress)
    }

    // Load client template when wrapper is first shown
    LaunchedEffect(Unit) {
        extensionViewModel.loadClientTemplate()
    }

    if (syncResults.isNotEmpty()) {
        // Show completion screen
        ClientSyncCompletionScreen(
            syncResults = syncResults,
            onFinish = {
                onSyncComplete(syncResults)
            },
        )
    } else {
        // Show validation screen
        ClientValidationScreen(
            invalidPayloads = invalidPayloads,
            clientTemplate = loadedClientTemplate,
            isUpdateAndSyncInProgress = isUpdateAndSyncInProgress,
            onBackPressed = onBackPressed,
            onValidationComplete = { updates ->
                extensionViewModel.updateMultiplePayloadsAndSync(updates) { results ->
                    onSyncComplete(results)
                }
            },
        )
    }
}
