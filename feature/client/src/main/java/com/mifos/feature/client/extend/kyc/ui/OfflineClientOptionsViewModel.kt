/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.client.extend.kyc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mifos.core.data.repository.SyncClientPayloadsRepository
import com.mifos.core.objects.client.ClientPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class OfflineClientOptionsViewModel @Inject constructor(
    private val syncClientPayloadsRepository: SyncClientPayloadsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<OfflineClientOptionsUiState>(OfflineClientOptionsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadClientPayload(clientPayloadId: Int) = viewModelScope.launch(Dispatchers.IO) {
        _uiState.value = OfflineClientOptionsUiState.Loading
        
        syncClientPayloadsRepository.allDatabaseClientPayload()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                { clientPayloads ->
                    val clientPayload = clientPayloads.find { it.id == clientPayloadId }
                    if (clientPayload != null) {
                        _uiState.value = OfflineClientOptionsUiState.Success(clientPayload)
                    } else {
                        _uiState.value = OfflineClientOptionsUiState.Error("Client payload not found")
                    }
                },
                { error ->
                    _uiState.value = OfflineClientOptionsUiState.Error(
                        error.message ?: "Failed to load client payload"
                    )
                }
            )
    }
}

sealed class OfflineClientOptionsUiState {
    data object Loading : OfflineClientOptionsUiState()
    data class Success(val clientPayload: ClientPayload) : OfflineClientOptionsUiState()
    data class Error(val message: String) : OfflineClientOptionsUiState()
} 