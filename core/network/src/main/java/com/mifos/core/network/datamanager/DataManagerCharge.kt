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

import com.mifos.core.model.objects.clients.Page
import com.mifos.core.network.BaseApiManager
import com.mifos.room.entities.client.ChargesEntity
import com.mifos.room.helper.ChargeDaoHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This DataManager is for Managing Charge API, In which Request is going to Server
 * and In Response, We are getting Charge API Observable Response using Retrofit2.
 * DataManagerCharge saving response in Database and response to Presenter as accordingly.
 *
 *
 * Created by Rajan Maurya on 4/7/16.
 */
@Singleton
class DataManagerCharge @Inject constructor(
    val mBaseApiManager: BaseApiManager,
//    val mDatabaseHelperCharge: DatabaseHelperCharge,
    val chargeDatabase: ChargeDaoHelper,
    private val prefManager: com.mifos.core.datastore.PrefManager,
) {
    /**
     * This Method Request the Charge API at
     * https://demo.openmf.org/fineract-provider/api/v1/clients/{clientId}/charges
     * and in response get the of the Charge Page that contains Charges list.
     *
     * @param clientId Client Id
     * @param offset   Offset From Which Position Charge List user want
     * @param limit    Maximum Limit of the Response Charge List Size
     * @return Page<Charge> Page of Charge in Which List Size is according to Limit and from
     * where position is Starting according to offset</Charge>>
     */
    suspend fun getClientCharges(
        clientId: Int,
        offset: Int,
        limit: Int,
    ): Flow<Page<ChargesEntity>> {
        return when (prefManager.userStatus) {
            false -> mBaseApiManager.chargeApi.getListOfCharges(clientId, offset, limit).map {
                chargeDatabase.saveClientCharges(it, clientId)
                it
            }

            true -> {
                /**
                 * Return Client Charges from DatabaseHelperClient only one time.
                 */
                if (offset == 0) {
                    chargeDatabase.readClientCharges(clientId)
                } else {
                    flow {
                        emit(Page())
                    }
                }
            }
        }
    }
}
