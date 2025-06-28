/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.network.datamanager

import com.mifos.core.datastore.PrefManager
import com.mifos.core.network.BaseApiManager
import com.mifos.core.objects.organisation.Office
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This DataManager is for Managing Offices API, In which Request is going to Server
 * and In Response, We are getting Offices API Observable Response using Retrofit2.
 * DataManagerOffices saving response in Database and response to Presenter as accordingly.
 *
 * Created by Rajan Maurya on 7/7/16.
 */
@Singleton
class DataManagerOffices @Inject constructor(
    private val baseApiManager: BaseApiManager,
    private val prefManager: PrefManager,
) {
    /**
     * return all List of Offices, with offline mode support
     */
    suspend fun offices(): List<Office> {
        return when (prefManager.userStatus) {
            false -> {
                // Online mode - fetch from server
                try {
                    baseApiManager.officeApi.allOffices()
                } catch (e: Exception) {
                    // If network fails, return empty list to allow offline creation
                    emptyList()
                }
            }
            true -> {
                // Offline mode - return empty list to allow offline client creation
                // In offline mode, office selection will show "Office data not available (Offline mode)"
                emptyList()
            }
        }
    }
}
