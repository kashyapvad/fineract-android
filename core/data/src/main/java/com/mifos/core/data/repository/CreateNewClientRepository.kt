/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.data.repository

import com.mifos.room.entities.client.ClientPayloadEntity
import com.mifos.room.entities.organisation.OfficeEntity
import com.mifos.room.entities.organisation.StaffEntity
import com.mifos.room.entities.templates.clients.ClientsTemplateEntity
import io.ktor.http.content.PartData
import kotlinx.coroutines.flow.Flow

/**
 * Created by Aditya Gupta on 10/08/23.
 */
interface CreateNewClientRepository {

    fun clientTemplate(): Flow<ClientsTemplateEntity>

    fun offices(): Flow<List<OfficeEntity>>

    fun getStaffInOffice(officeId: Int): Flow<List<StaffEntity>>

    suspend fun createClient(clientPayload: ClientPayloadEntity): Int?

    suspend fun uploadClientImage(id: Int, file: PartData?)
}
