/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.client.extend.preSyncValidation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mifos.core.data.repository.SyncClientPayloadsRepository
import com.mifos.core.network.datamanager.DataManagerClient
import com.mifos.core.objects.client.ClientPayload
import com.mifos.core.objects.templates.clients.ClientsTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class PreSyncValidationViewModel @Inject constructor(
    private val syncRepository: SyncClientPayloadsRepository,
    private val clientDataManager: DataManagerClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PreSyncValidationUiState>(PreSyncValidationUiState.Loading)
    val uiState: StateFlow<PreSyncValidationUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var clientTemplate: ClientsTemplate? = null
    private var allPayloads = listOf<ClientPayload>()
    private var filterByClientId: Int? = null

    fun loadInvalidClientPayloads(specificClientId: Int? = null) {
        println("🔍 PreSyncValidationViewModel: Loading invalid payloads, specificClientId: $specificClientId")
        filterByClientId = specificClientId
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                syncRepository.allDatabaseClientPayload()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { payloads ->
                            allPayloads = payloads
                            println("📋 PreSyncValidationViewModel: Loaded ${payloads.size} total payloads")
                            updateUiStateWithInvalidPayloads()
                            _isLoading.value = false
                        },
                        { error ->
                            _uiState.value = PreSyncValidationUiState.Error(
                                error.message ?: "Failed to load client payloads"
                            )
                            _isLoading.value = false
                        }
                    )
            } catch (e: Exception) {
                _uiState.value = PreSyncValidationUiState.Error(
                    e.message ?: "An unexpected error occurred"
                )
                _isLoading.value = false
            }
        }
    }

    fun loadClientTemplate() {
        viewModelScope.launch {
            try {
                clientDataManager.clientTemplate
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { template: ClientsTemplate ->
                            clientTemplate = template
                            updateUiStateWithInvalidPayloads()
                        },
                        { error: Throwable ->
                            // Template loading failed, continue without template
                            // (office options will not be available)
                            updateUiStateWithInvalidPayloads()
                        }
                    )
            } catch (e: Exception) {
                // Continue without template
                updateUiStateWithInvalidPayloads()
            }
        }
    }

    fun updateClientPayload(updatedPayload: ClientPayload) {
        viewModelScope.launch {
            try {
                syncRepository.updateClientPayload(updatedPayload)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { _: ClientPayload ->
                            // Update local list and refresh UI
                            allPayloads = allPayloads.map { payload ->
                                if (payload.id == updatedPayload.id) updatedPayload else payload
                            }
                            updateUiStateWithInvalidPayloads()
                        },
                        { error: Throwable ->
                            // Handle update error - could show toast or update error state
                        }
                    )
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    private fun updateUiStateWithInvalidPayloads() {
        println("🔍 PreSyncValidationViewModel: Filtering payloads, filterByClientId: $filterByClientId")
        
        var payloadsToCheck = allPayloads
        
        // ADDED: Filter by specific client ID for individual sync
        if (filterByClientId != null) {
            payloadsToCheck = allPayloads.filter { it.id == filterByClientId }
            println("🎯 PreSyncValidationViewModel: Filtered to ${payloadsToCheck.size} payloads for client ID $filterByClientId")
        }
        
        val invalidPayloads = payloadsToCheck.filter { payload ->
            // Only consider client creation payloads (not existing client updates)
            // Check if this is a new client creation by looking for missing critical data
            val isNewClientCreation = payload.id == null || payload.clientCreationTime != null
            
            // Only include payloads that are new client creations AND have validation issues
            // Be more strict about what constitutes validation issues
            val hasValidationIssues = (payload.officeId == null || payload.officeId == 0)
            // Note: Removed genderId validation as it's optional in many implementations
            // and the client creation screen properly sets it when available
            
            val isInvalid = isNewClientCreation && hasValidationIssues
            println("💳 PreSyncValidationViewModel: Client ${payload.firstname} ${payload.lastname} (ID: ${payload.id}) - isInvalid: $isInvalid")
            isInvalid
        }

        println("⚠️ PreSyncValidationViewModel: Found ${invalidPayloads.size} invalid payloads")
        _uiState.value = PreSyncValidationUiState.ShowInvalidPayloads(
            invalidPayloads = invalidPayloads,
            clientTemplate = clientTemplate,
        )
    }
} 