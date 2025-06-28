/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.client.extend.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mifos.core.data.repository.SyncClientPayloadsRepository
import com.mifos.core.objects.client.ClientPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

/**
 * Navigation helper for client sync with validation
 */
@HiltViewModel
class ClientSyncNavigationHelper @Inject constructor(
    private val syncRepository: SyncClientPayloadsRepository,
) : ViewModel() {

    var shouldShowPreSyncValidation by mutableStateOf(false)
        private set

    /**
     * Check if pre-sync validation is needed
     * Returns true if validation screen should be shown, false if can proceed directly to sync
     */
    fun checkIfValidationNeeded(onResult: (needsValidation: Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                syncRepository.allDatabaseClientPayload()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { payloads ->
                            val needsValidation = hasInvalidPayloads(payloads)
                            shouldShowPreSyncValidation = needsValidation
                            onResult(needsValidation)
                        },
                        { error ->
                            // If error, assume no validation needed and proceed with normal sync
                            onResult(false)
                        }
                    )
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    private fun hasInvalidPayloads(payloads: List<ClientPayload>): Boolean {
        return payloads.any { payload ->
            // Only consider client creation payloads (not existing client updates)
            val isNewClientCreation = payload.clientCreationTime != null ||
                                      (payload.officeId == null || payload.officeId == 0)
            
            // Only include payloads that are new client creations AND have validation issues
            val hasValidationIssues = (payload.officeId == null || payload.officeId == 0)
            // Note: Removed genderId validation as it's optional in many implementations
            
            isNewClientCreation && hasValidationIssues
        }
    }

    fun markValidationCompleted() {
        shouldShowPreSyncValidation = false
    }
}

/**
 * Extension point for handling client sync navigation
 */
interface ClientSyncNavigationExtension {
    /**
     * Handle sync request with optional pre-validation
     * @param onNavigateToValidation callback to navigate to validation screen
     * @param onProceedToSync callback to proceed directly to sync
     */
    fun handleSyncRequest(
        onNavigateToValidation: () -> Unit,
        onProceedToSync: () -> Unit,
    )
}

/**
 * Default implementation that checks for validation needs
 */
class DefaultClientSyncNavigationExtension(
    private val navigationHelper: ClientSyncNavigationHelper,
) : ClientSyncNavigationExtension {

    override fun handleSyncRequest(
        onNavigateToValidation: () -> Unit,
        onProceedToSync: () -> Unit,
    ) {
        navigationHelper.checkIfValidationNeeded { needsValidation ->
            if (needsValidation) {
                onNavigateToValidation()
            } else {
                onProceedToSync()
            }
        }
    }
} 