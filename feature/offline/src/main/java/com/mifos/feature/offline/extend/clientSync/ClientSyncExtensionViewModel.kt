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

import android.util.Log
import androidx.lifecycle.ViewModel
import com.mifos.core.common.extend.events.ClientEventBus
import com.mifos.core.common.extend.events.ClientEventListener
import com.mifos.core.common.extend.events.ClientCreatedEvent
import com.mifos.core.common.extend.events.ClientSyncCompletedEvent
import com.mifos.core.common.utils.FileUtils.LOG_TAG
import com.mifos.core.data.repository.SyncClientPayloadsRepository
import com.mifos.core.data.services.extend.ClientCreationHelper
import com.mifos.core.network.datamanager.DataManagerClient
import com.mifos.core.objects.client.Client
import com.mifos.core.objects.client.ClientPayload
import com.mifos.core.objects.templates.clients.ClientsTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

/**
 * Extension ViewModel for client sync validation and batch updates.
 * This keeps the extension logic separate from the upstream ViewModel.
 * Implements event-driven sync completion and bridge pattern.
 */
@HiltViewModel
class ClientSyncExtensionViewModel
    @Inject
    constructor(
        private val repository: SyncClientPayloadsRepository,
        private val clientDataManager: DataManagerClient,
        private val clientCreationHelper: ClientCreationHelper,
    ) : ViewModel(), SyncExtensionBridge, ClientEventListener {
    
    init {
        // Subscribe to client events for completion handling
        ClientEventBus.subscribe(this)
    }

    override fun onCleared() {
        super.onCleared()
        // Unsubscribe from events
        ClientEventBus.unsubscribe(this)
    }
        private val _clientTemplate = MutableStateFlow<ClientsTemplate?>(null)
        val clientTemplate: StateFlow<ClientsTemplate?> = _clientTemplate.asStateFlow()

        private val _syncResults = MutableStateFlow<List<ClientSyncResult>>(emptyList())
        val syncResults: StateFlow<List<ClientSyncResult>> = _syncResults.asStateFlow()

        private val _isUpdateAndSyncInProgress = MutableStateFlow(false)
        val isUpdateAndSyncInProgress: StateFlow<Boolean> = _isUpdateAndSyncInProgress.asStateFlow()
        
        // Extension state management
        private val _extensionState = MutableStateFlow(
            ClientSyncExtensionState(
                syncStates = emptyMap(),
                isBulkSyncing = false,
                isIndividualSyncInProgress = false
            )
        )
        override val extensionState: StateFlow<Any?> = _extensionState.asStateFlow()
        
        // Pending payload deletions (waiting for KYC sync completion)
        private val pendingDeletions = mutableMapOf<Int, ClientPayload>()
        
        // Track extension sync completion callbacks
        private val pendingCompletions = mutableMapOf<Int, ExtensionCompletionInfo>()
        
        data class ExtensionCompletionInfo(
            val payload: ClientPayload,
            val syncResults: MutableList<ClientSyncResult>,
            val onComplete: () -> Unit
        )

        fun loadClientTemplate() {
            clientDataManager.clientTemplate
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { template: ClientsTemplate ->
                        _clientTemplate.value = template
                    },
                    { error: Throwable ->
                        Log.e(LOG_TAG, "Failed to load client template: ${error.message}")
                    },
                )
        }

        fun updateMultiplePayloadsAndSync(
            updates: List<Triple<ClientPayload, Int?, Int?>>,
            onSyncComplete: (List<ClientSyncResult>) -> Unit,
        ) {
            if (updates.isEmpty()) {
                onSyncComplete(emptyList())
                return
            }

            // Prevent duplicate operations
            if (_isUpdateAndSyncInProgress.value) {
                Log.d(LOG_TAG, "Update & Sync already in progress, ignoring request")
                return
            }
            
        _isUpdateAndSyncInProgress.value = true
        updateExtensionState { it.copy(isBulkSyncing = true) } // Sync UI state
        _syncResults.value = emptyList() // Clear previous results

            var completedUpdates = 0
            val totalUpdates = updates.size
            val syncResults = mutableListOf<ClientSyncResult>()

            updates.forEach { (payload, officeId, genderId) ->
                val updatedPayload =
                    payload.copy().apply {
                        if (officeId != null && officeId > 0) {
                            this.officeId = officeId
                        }
                        if (genderId != null && genderId > 0) {
                            this.genderId = genderId
                        }
                    }

                repository.updateClientPayload(updatedPayload)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        object : Subscriber<ClientPayload>() {
                            override fun onCompleted() {}

                            override fun onError(e: Throwable) {
                                Log.e(LOG_TAG, "Failed to update client payload: ${e.message}")
                                syncResults.add(
                                    ClientSyncResult(
                                        clientName = "${payload.firstname} ${payload.lastname}",
                                        isSuccess = false,
                                        errorMessage = "Update failed: ${e.message}",
                                    ),
                                )
                                completedUpdates++
                                checkCompletion(completedUpdates, totalUpdates, syncResults, onSyncComplete)
                            }

                            override fun onNext(clientPayload: ClientPayload) {
                                // Now sync the updated payload
                                syncSingleClient(clientPayload, syncResults) {
                                    completedUpdates++
                                    checkCompletion(completedUpdates, totalUpdates, syncResults, onSyncComplete)
                                }
                            }
                        },
                    )
            }
        }

        private fun syncSingleClient(
            payload: ClientPayload,
            syncResults: MutableList<ClientSyncResult>,
            onComplete: () -> Unit,
        ) {
            repository.createClient(payload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    object : Subscriber<Client>() {
                        override fun onCompleted() {}

                        override fun onError(e: Throwable) {
                            syncResults.add(
                                ClientSyncResult(
                                    clientName = "${payload.firstname} ${payload.lastname}",
                                    isSuccess = false,
                                    errorMessage = e.message,
                                ),
                            )
                            onComplete()
                        }

                        override fun onNext(client: Client) {
                            Log.d(LOG_TAG, "Extension client created - ${payload.firstname} ${payload.lastname}")
                            
                            // EVENT-DRIVEN APPROACH: Fire event and wait for completion
                            val serverClientId = client.clientId ?: client.id
                            val payloadId = payload.id
                            
                            if (payloadId != null && serverClientId > 0) {
                                // Store completion context for event-driven handling
                                pendingDeletions[payloadId] = payload
                                pendingCompletions[payloadId] = ExtensionCompletionInfo(
                                    payload = payload,
                                    syncResults = syncResults,
                                    onComplete = onComplete
                                )
                                
                                // Fire the event - completion will be handled by onClientSyncCompleted
                                clientCreationHelper.notifyClientCreated(serverClientId, payloadId, "extension_sync")
                            } else {
                                // No KYC data - immediate completion
                                payload.id?.let { id ->
                                    payload.clientCreationTime?.let { time ->
                                        deletePayload(id, time)
                                    }
                                }
                                
                                syncResults.add(
                                    ClientSyncResult(
                                        clientName = "${payload.firstname} ${payload.lastname}",
                                        isSuccess = true,
                                    ),
                                )
                                onComplete()
                            }
                        }
                    },
                )
        }

        private fun deletePayload(
            id: Int,
            clientCreationTime: Long,
        ) {
            repository.deleteAndUpdatePayloads(id, clientCreationTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { /* Success - payload deleted */ },
                    { error -> Log.e(LOG_TAG, "Failed to delete payload: ${error.message}") },
                )
        }

        private fun checkCompletion(
            completedUpdates: Int,
            totalUpdates: Int,
            syncResults: MutableList<ClientSyncResult>,
            onSyncComplete: (List<ClientSyncResult>) -> Unit,
        ) {
            if (completedUpdates == totalUpdates) {
                Log.d(LOG_TAG, "Extension Update & Sync completed for all $totalUpdates clients")
                _isUpdateAndSyncInProgress.value = false // Reset loading state
                updateExtensionState { it.copy(isBulkSyncing = false) } // Sync UI state
                _syncResults.value = syncResults.toList()
                onSyncComplete(syncResults.toList())
            }
        }
        
        // Bridge interface implementations
        override fun syncIndividualClient(payload: ClientPayload) {
            val payloadId = payload.id ?: return
            
            if (_isUpdateAndSyncInProgress.value) {
                Log.d(LOG_TAG, "Extension sync in progress, cannot start individual sync")
                return
            }
            
            if (isIndividualSyncInProgress()) {
                Log.d(LOG_TAG, "Another individual sync in progress, please wait")
                return
            }
            
            // Pre-validation to prevent 400 errors
            val validationErrors = validateClientPayload(payload)
            if (validationErrors.isNotEmpty()) {
                Log.d(LOG_TAG, "Individual sync validation failed: ${validationErrors.joinToString(", ")}")
                updateSyncState(payloadId, SyncStatus.FAILED, error = "Validation errors: ${validationErrors.joinToString(", ")}")
                return
            }
            
            Log.d(LOG_TAG, "Starting individual sync for client ${payload.firstname} ${payload.lastname}")
            
            // Update extension state
            updateExtensionState { it.copy(isIndividualSyncInProgress = true) }
            updateSyncState(payloadId, SyncStatus.SYNCING, "Starting sync...")
            
            // Use existing repository logic
            repository.createClient(payload)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { client -> onIndividualClientSyncSuccess(payloadId, client, payload) },
                    { error -> 
                        Log.e(LOG_TAG, "Individual client sync failed for payload $payloadId", error)
                        updateExtensionState { it.copy(isIndividualSyncInProgress = false) }
                        updateSyncState(payloadId, SyncStatus.FAILED, error = error.message)
                    }
                )
        }
        
        override fun validateClientPayload(payload: ClientPayload): List<String> {
            val errors = mutableListOf<String>()
            
            // Check required fields
            if (payload.officeId == null || payload.officeId == 0) {
                errors.add("Office ID is required and must be greater than 0")
            }
            
            if (payload.firstname.isNullOrBlank()) {
                errors.add("First name is required")
            }
            
            if (payload.lastname.isNullOrBlank()) {
                errors.add("Last name is required")
            }
            
            return errors
        }
        
        override fun isIndividualSyncInProgress(): Boolean {
            return (_extensionState.value as? ClientSyncExtensionState)?.isIndividualSyncInProgress == true
        }
        
        override fun shouldBlockBulkSync(): Boolean {
            return _isUpdateAndSyncInProgress.value || isIndividualSyncInProgress()
        }
        
        private fun onIndividualClientSyncSuccess(payloadId: Int, client: Client, payload: ClientPayload) {
            val serverClientId = client.clientId ?: client.id
            Log.d(LOG_TAG, "Individual client sync success - payloadId: $payloadId, serverClientId: $serverClientId")
            
            // Update state to KYC syncing
            updateSyncState(payloadId, SyncStatus.KYC_SYNCING, "Syncing KYC data...")
            
            // Store for event-driven completion
            pendingDeletions[payloadId] = payload
            
            // Fire the event for KYC sync
            clientCreationHelper.notifyClientCreated(serverClientId, payloadId, "individual_sync")
        }
        
        private fun updateSyncState(payloadId: Int, status: SyncStatus, progress: String = "", error: String? = null) {
            val currentState = _extensionState.value as ClientSyncExtensionState
            val updatedStates = currentState.syncStates.toMutableMap()
            updatedStates[payloadId] = ClientSyncState(payloadId, status, progress, error)
            
            updateExtensionState { 
                it.copy(syncStates = updatedStates)
            }
            
            Log.d(LOG_TAG, "Individual sync state updated - payloadId: $payloadId, status: $status")
        }
        
        private fun updateExtensionState(update: (ClientSyncExtensionState) -> ClientSyncExtensionState) {
            val currentState = _extensionState.value as ClientSyncExtensionState
            _extensionState.value = update(currentState)
        }
        
        private fun deleteIndividualPayload(payloadId: Int) {
            repository.deleteAndUpdatePayloads(payloadId, 0L)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { _ ->
                        Log.d(LOG_TAG, "Individual payload deleted successfully: $payloadId")
                        updateExtensionState { it.copy(isIndividualSyncInProgress = false) }
                        updateSyncState(payloadId, SyncStatus.SUCCESS, "Successfully synced!")
                    },
                    { error ->
                        Log.e(LOG_TAG, "Failed to delete individual payload: $payloadId", error)
                        updateExtensionState { it.copy(isIndividualSyncInProgress = false) }
                        updateSyncState(payloadId, SyncStatus.FAILED, error = "Failed to delete: ${error.message}")
                    }
                )
        }

        // Event listener implementations
        override fun onClientCreated(event: ClientCreatedEvent) {
            // Not needed for this ViewModel - we only care about completion
        }

        override fun onClientSyncCompleted(event: ClientSyncCompletedEvent) {
            val localClientId = event.localClientId ?: return
            val payload = pendingDeletions[localClientId] ?: return
            
            // Remove from pending deletions
            pendingDeletions.remove(localClientId)
            
            if (event.success) {
                Log.d(LOG_TAG, "Extension KYC sync completed for client ${event.serverClientId}")
                
                // Delete payload and update state based on sync type
                if (event.source == "individual_sync") {
                    deleteIndividualPayload(localClientId)
                } else if (event.source == "extension_sync") {
                    // Extension sync - delete payload and call completion
                    val completionInfo = pendingCompletions[localClientId]
                    if (completionInfo != null) {
                        pendingCompletions.remove(localClientId)
                        
                        // Add success result to sync results
                        completionInfo.syncResults.add(
                            ClientSyncResult(
                                clientName = "${completionInfo.payload.firstname} ${completionInfo.payload.lastname}",
                                isSuccess = true
                            )
                        )
                        
                        // Delete payload
                        payload.id?.let { id ->
                            payload.clientCreationTime?.let { time ->
                                deletePayload(id, time)
                            }
                        }
                        
                        // Call completion callback (this will trigger checkCompletion)
                        completionInfo.onComplete()
                    } else {
                        Log.w(LOG_TAG, "No completion info found for extension sync payload $localClientId")
                    }
                } else {
                    // Other sync types - just delete payload
                    payload.id?.let { id ->
                        payload.clientCreationTime?.let { time ->
                            deletePayload(id, time)
                        }
                    }
                }
            } else {
                Log.e(LOG_TAG, "Extension KYC sync failed for client ${event.serverClientId}: ${event.error}")
                
                if (event.source == "individual_sync") {
                    updateExtensionState { it.copy(isIndividualSyncInProgress = false) }
                    updateSyncState(localClientId, SyncStatus.FAILED, error = event.error)
                } else if (event.source == "extension_sync") {
                    // Extension sync failed - add failure result and call completion
                    val completionInfo = pendingCompletions[localClientId]
                    if (completionInfo != null) {
                        pendingCompletions.remove(localClientId)
                        
                        // Add failure result to sync results
                        completionInfo.syncResults.add(
                            ClientSyncResult(
                                clientName = "${completionInfo.payload.firstname} ${completionInfo.payload.lastname}",
                                isSuccess = false,
                                errorMessage = event.error
                            )
                        )
                        
                        // Call completion callback (this will trigger checkCompletion)
                        completionInfo.onComplete()
                    } else {
                        Log.w(LOG_TAG, "No completion info found for failed extension sync payload $localClientId")
                    }
                }
                // For other sync types, error handling is managed by the calling method
            }
        }
    }
