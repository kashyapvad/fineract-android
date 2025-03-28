/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.data.repositoryImp

import com.mifos.core.data.repository.SyncCenterPayloadsRepository
import com.mifos.core.network.datamanager.DataManagerCenter
import com.mifos.room.entities.center.CenterPayloadEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Aditya Gupta on 16/08/23.
 */
class SyncCenterPayloadsRepositoryImp(
    private val dataManagerCenter: DataManagerCenter,
) : SyncCenterPayloadsRepository {

    override fun allDatabaseCenterPayload(): Flow<List<CenterPayloadEntity>> {
        return dataManagerCenter.allDatabaseCenterPayload
    }

    override suspend fun createCenter(centerPayload: CenterPayloadEntity?) {
        dataManagerCenter.createCenter(centerPayload)
    }

    override fun deleteAndUpdateCenterPayloads(id: Int): Flow<List<CenterPayloadEntity>> {
        return dataManagerCenter.deleteAndUpdateCenterPayloads(id)
    }

    override suspend fun updateCenterPayload(centerPayload: CenterPayloadEntity) {
        dataManagerCenter.updateCenterPayload(centerPayload)
    }
}
