/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.client.extend.clientList

import com.mifos.core.data.repository.ClientListRepository
import com.mifos.core.data.repository.SyncClientPayloadsRepository
import com.mifos.core.objects.client.Client
import com.mifos.core.objects.client.ClientPayload
import com.mifos.core.objects.client.Page
import com.mifos.feature.client.clientList.presentation.ClientListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Extension object that handles complex offline client loading logic
 */
object ClientListViewModelExtension {
    
    /**
     * Loads clients from database with both synced clients and pending payloads
     * This handles the complex RxJava subscriber logic that was cluttering the main ViewModel
     */
    fun loadClientsFromDb(
        repository: ClientListRepository,
        syncClientPayloadsRepository: SyncClientPayloadsRepository,
        uiStateFlow: MutableStateFlow<ClientListUiState>
    ) {
        try {
            // Set loading state immediately
            uiStateFlow.value = ClientListUiState.Empty
            
            // Use structured concurrency approach
            var syncedClients: List<Client> = emptyList()
            var pendingPayloads: List<ClientPayload> = emptyList()
            var syncedLoaded = false
            var payloadsLoaded = false
            
            // Helper function to update final state
            fun updateFinalState() {
                uiStateFlow.value = when {
                    ClientListExtension.shouldShowPendingClients(syncedClients, pendingPayloads) -> {
                        ClientListUiState.ClientListDbWithPending(
                            syncedClients = syncedClients,
                            pendingClients = pendingPayloads
                        )
                    }
                    else -> {
                        ClientListUiState.Error("No clients found. Please sync data or create new clients.")
                    }
                }
            }
            
            // Load synced clients
            repository.allDatabaseClients()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : Subscriber<Page<Client>>() {
                    override fun onCompleted() {
                        syncedLoaded = true
                        checkAndUpdateState()
                    }

                    override fun onError(error: Throwable) {
                        // If synced clients fail, continue with just pending clients
                        syncedClients = emptyList()
                        syncedLoaded = true
                        checkAndUpdateState()
                    }

                    override fun onNext(clients: Page<Client>) {
                        syncedClients = clients.pageItems
                    }
                    
                    private fun checkAndUpdateState() {
                        if (syncedLoaded && payloadsLoaded) {
                            updateFinalState()
                        }
                    }
                })
            
            // Load pending payloads
            syncClientPayloadsRepository.allDatabaseClientPayload()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : Subscriber<List<ClientPayload>>() {
                    override fun onCompleted() {
                        payloadsLoaded = true
                        checkAndUpdateState()
                    }

                    override fun onError(error: Throwable) {
                        // If pending payloads fail, continue with just synced clients
                        pendingPayloads = emptyList()
                        payloadsLoaded = true
                        checkAndUpdateState()
                    }

                    override fun onNext(payloads: List<ClientPayload>) {
                        pendingPayloads = payloads
                    }
                    
                    private fun checkAndUpdateState() {
                        if (syncedLoaded && payloadsLoaded) {
                            updateFinalState()
                        }
                    }
                })
            
        } catch (e: Exception) {
            uiStateFlow.value = ClientListUiState.Error("Failed to load clients: ${e.message}")
        }
    }
} 