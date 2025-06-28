/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.offline.extend.kyc

import android.util.Log
import androidx.lifecycle.ViewModel
import com.mifos.core.common.utils.FileUtils.LOG_TAG
import com.mifos.core.data.repository.extend.SyncKycRepository
import com.mifos.core.datastore.PrefManager
import com.mifos.core.objects.client.extend.ClientKycPayload
import com.mifos.core.objects.client.extend.GuarantorKycPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rx.Observer
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

/**
 * ViewModel for KYC sync operations.
 * 
 * Handles synchronization of Client KYC and Guarantor KYC data
 * following the same patterns as SyncClientPayloadsViewModel.
 */
@HiltViewModel
class SyncKycViewModel @Inject constructor(
    private val syncKycRepository: SyncKycRepository,
    private val prefManager: PrefManager,
) : ViewModel() {

    private val _syncKycUiState = MutableStateFlow<SyncKycUiState>(SyncKycUiState.Loading)
    val syncKycUiState: StateFlow<SyncKycUiState> = _syncKycUiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var mClientKycPayloads: MutableList<ClientKycPayload> = mutableListOf()
    private var mGuarantorKycPayloads: MutableList<GuarantorKycPayload> = mutableListOf()
    private var mClientKycSyncIndex = 0
    private var mGuarantorKycSyncIndex = 0

    fun getUserStatus(): Boolean {
        return prefManager.userStatus
    }

    fun refreshKycPayloads() {
        _isRefreshing.value = true
        loadKycPayloads()
        _isRefreshing.value = false
    }

    fun loadKycPayloads() {
        _syncKycUiState.value = SyncKycUiState.Loading
        
        // Load both client and guarantor KYC payloads
        syncKycRepository.getAllClientKycPayloads()
            .zipWith(syncKycRepository.getAllGuarantorKycPayloads()) { clientKyc, guarantorKyc ->
                Pair(clientKyc, guarantorKyc)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Subscriber<Pair<List<ClientKycPayload>, List<GuarantorKycPayload>>>() {
                override fun onCompleted() {}
                
                override fun onError(e: Throwable) {
                    _syncKycUiState.value = SyncKycUiState.Error(e.message ?: "Unknown error")
                }

                override fun onNext(kycPayloads: Pair<List<ClientKycPayload>, List<GuarantorKycPayload>>) {
                    mClientKycPayloads = kycPayloads.first.toMutableList()
                    mGuarantorKycPayloads = kycPayloads.second.toMutableList()
                    _syncKycUiState.value = SyncKycUiState.Success(
                        clientKycPayloads = mClientKycPayloads,
                        guarantorKycPayloads = mGuarantorKycPayloads
                    )
                }
            })
    }

    fun syncKycPayloads() {
        // First sync client KYC payloads
        if (mClientKycPayloads.isNotEmpty()) {
            syncClientKycPayloads()
        } else if (mGuarantorKycPayloads.isNotEmpty()) {
            syncGuarantorKycPayloads()
        }
    }

    private fun syncClientKycPayloads() {
        for (i in mClientKycPayloads.indices) {
            val payload = mClientKycPayloads[i]
            if (payload.errorMessage == null && payload.syncStatus != "SYNCED") {
                syncClientKycPayload(payload)
                mClientKycSyncIndex = i
                break
            } else {
                payload.errorMessage?.let { Log.d(LOG_TAG, it) }
            }
        }
    }

    private fun syncGuarantorKycPayloads() {
        for (i in mGuarantorKycPayloads.indices) {
            val payload = mGuarantorKycPayloads[i]
            if (payload.errorMessage == null && payload.syncStatus != "SYNCED") {
                syncGuarantorKycPayload(payload)
                mGuarantorKycSyncIndex = i
                break
            } else {
                payload.errorMessage?.let { Log.d(LOG_TAG, it) }
            }
        }
    }

    private fun syncClientKycPayload(clientKycPayload: ClientKycPayload) {
        _syncKycUiState.value = SyncKycUiState.Loading
        
        syncKycRepository.syncClientKyc(clientKycPayload)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Observer<Map<String, Any>> {
                override fun onCompleted() {}
                
                override fun onError(e: Throwable) {
                    clientKycPayload.errorMessage = e.message
                    clientKycPayload.syncStatus = "ERROR"
                    updateClientKycPayload(clientKycPayload)
                }

                override fun onNext(clientKycData: Map<String, Any>) {
                    // Successfully synced - mark as synced and continue
                    clientKycPayload.syncStatus = "SYNCED"
                    clientKycPayload.serverKycId = (clientKycData["id"] as? Number)?.toLong()
                    deleteClientKycPayload(clientKycPayload.id!!)
                }
            })
    }

    private fun syncGuarantorKycPayload(guarantorKycPayload: GuarantorKycPayload) {
        _syncKycUiState.value = SyncKycUiState.Loading
        
        syncKycRepository.syncGuarantorKyc(guarantorKycPayload)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Observer<Map<String, Any>> {
                override fun onCompleted() {}
                
                override fun onError(e: Throwable) {
                    guarantorKycPayload.errorMessage = e.message
                    guarantorKycPayload.syncStatus = "ERROR"
                    updateGuarantorKycPayload(guarantorKycPayload)
                }

                override fun onNext(guarantorKycData: Map<String, Any>) {
                    // Successfully synced - mark as synced and continue
                    guarantorKycPayload.syncStatus = "SYNCED"
                    guarantorKycPayload.serverGuarantorKycId = (guarantorKycData["id"] as? Number)?.toLong()
                    deleteGuarantorKycPayload(guarantorKycPayload.id!!)
                }
            })
    }

    private fun updateClientKycPayload(clientKycPayload: ClientKycPayload) {
        syncKycRepository.updateClientKycPayload(clientKycPayload)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Subscriber<ClientKycPayload>() {
                override fun onCompleted() {}
                
                override fun onError(e: Throwable) {
                    _syncKycUiState.value = SyncKycUiState.Error(e.message ?: "Update failed")
                }

                override fun onNext(updatedPayload: ClientKycPayload) {
                    mClientKycPayloads[mClientKycSyncIndex] = updatedPayload
                    mClientKycSyncIndex += 1
                    
                    // Continue with next payload or move to guarantor KYC
                    if (mClientKycSyncIndex < mClientKycPayloads.size) {
                        syncClientKycPayloads()
                    } else {
                        syncGuarantorKycPayloads()
                    }
                }
            })
    }

    private fun updateGuarantorKycPayload(guarantorKycPayload: GuarantorKycPayload) {
        syncKycRepository.updateGuarantorKycPayload(guarantorKycPayload)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Subscriber<GuarantorKycPayload>() {
                override fun onCompleted() {}
                
                override fun onError(e: Throwable) {
                    _syncKycUiState.value = SyncKycUiState.Error(e.message ?: "Update failed")
                }

                override fun onNext(updatedPayload: GuarantorKycPayload) {
                    mGuarantorKycPayloads[mGuarantorKycSyncIndex] = updatedPayload
                    mGuarantorKycSyncIndex += 1
                    
                    if (mGuarantorKycSyncIndex < mGuarantorKycPayloads.size) {
                        syncGuarantorKycPayloads()
                    }
                }
            })
    }

    private fun deleteClientKycPayload(id: Int) {
        syncKycRepository.deleteClientKycPayload(id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Observer<List<ClientKycPayload>> {
                override fun onCompleted() {}
                
                override fun onError(e: Throwable) {
                    _syncKycUiState.value = SyncKycUiState.Error(e.message ?: "Delete failed")
                }

                override fun onNext(remainingPayloads: List<ClientKycPayload>) {
                    mClientKycPayloads = remainingPayloads.toMutableList()
                    mClientKycSyncIndex = 0
                    
                    // Continue with remaining payloads or move to guarantor KYC
                    if (mClientKycPayloads.isNotEmpty()) {
                        syncClientKycPayloads()
                    } else {
                        syncGuarantorKycPayloads()
                    }
                }
            })
    }

    private fun deleteGuarantorKycPayload(id: Int) {
        syncKycRepository.deleteGuarantorKycPayload(id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Observer<List<GuarantorKycPayload>> {
                override fun onCompleted() {}
                
                override fun onError(e: Throwable) {
                    _syncKycUiState.value = SyncKycUiState.Error(e.message ?: "Delete failed")
                }

                override fun onNext(remainingPayloads: List<GuarantorKycPayload>) {
                    mGuarantorKycPayloads = remainingPayloads.toMutableList()
                    mGuarantorKycSyncIndex = 0
                    
                    if (mGuarantorKycPayloads.isNotEmpty()) {
                        syncGuarantorKycPayloads()
                    } else {
                        // All KYC payloads synced - refresh the UI
                        loadKycPayloads()
                    }
                }
            })
    }

    /**
     * Update KYC payloads with real server client ID after client sync
     * This is called from the client sync process to resolve dependencies
     */
    fun updateKycPayloadsWithNewClientId(oldClientId: Int, serverClientId: Int) {
        syncKycRepository.updateKycPayloadsWithNewClientId(oldClientId, serverClientId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Observer<Boolean> {
                override fun onCompleted() {}
                
                override fun onError(e: Throwable) {
                    Log.e(LOG_TAG, "Failed to update KYC payloads with server client ID", e)
                }

                override fun onNext(success: Boolean) {
                    if (success) {
                        Log.d(LOG_TAG, "Successfully updated KYC payloads with server client ID: $serverClientId")
                    }
                }
            })
    }
} 