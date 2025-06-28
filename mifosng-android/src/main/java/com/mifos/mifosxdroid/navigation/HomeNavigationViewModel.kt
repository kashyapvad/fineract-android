/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.mifosxdroid.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mifos.core.datastore.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeNavigationViewModel @Inject constructor(
    private val prefManager: PrefManager,
) : ViewModel() {

    private val _userStatus = MutableStateFlow(prefManager.userStatus)
    val userStatus = _userStatus.asStateFlow()

    fun updateUserStatus(status: Boolean) = viewModelScope.launch {
        prefManager.updateUserStatus(status)
        _userStatus.value = status
    }
} 