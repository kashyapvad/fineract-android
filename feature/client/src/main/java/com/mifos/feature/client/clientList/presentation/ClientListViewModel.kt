/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.client.clientList.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mifos.core.data.repository.ClientListRepository
import com.mifos.core.data.repository.SyncClientPayloadsRepository
import com.mifos.core.datastore.PrefManager
import com.mifos.feature.client.extend.clientList.ClientListViewModelExtension
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Aditya Gupta on 21/02/24.
 */

@HiltViewModel
class ClientListViewModel @Inject constructor(
    private val repository: ClientListRepository,
    private val syncClientPayloadsRepository: SyncClientPayloadsRepository,
    private val prefManager: PrefManager,
) : ViewModel() {

    private val _clientListUiState = MutableStateFlow<ClientListUiState>(ClientListUiState.Empty)
    val clientListUiState = _clientListUiState.asStateFlow()

    init {
        getClientList()
    }

    // for refresh feature
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun refreshClientList() {
        _isRefreshing.value = true
        getClientList()
        _isRefreshing.value = false
    }

    fun getClientList() {
        if (prefManager.userStatus) {
            loadClientsFromDb()
        } else {
            loadClientsFromApi()
        }
    }

    private fun loadClientsFromApi() = viewModelScope.launch(Dispatchers.IO) {
        val response = repository.getAllClients()
        _clientListUiState.value = ClientListUiState.ClientListApi(response)
    }

    private fun loadClientsFromDb() = viewModelScope.launch(Dispatchers.IO) {
        // Extension: Use extension to handle complex offline loading logic
        ClientListViewModelExtension.loadClientsFromDb(
            repository = repository,
            syncClientPayloadsRepository = syncClientPayloadsRepository,
            uiStateFlow = _clientListUiState
        )
    }
}
