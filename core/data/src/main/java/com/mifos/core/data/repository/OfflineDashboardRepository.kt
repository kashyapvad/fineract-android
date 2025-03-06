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

import com.mifos.core.entity.accounts.savings.SavingsAccountTransactionRequest
import com.mifos.core.entity.client.ClientPayload
import com.mifos.room.entities.accounts.loans.LoanRepaymentRequest
import com.mifos.room.entities.center.CenterPayload
import com.mifos.room.entities.group.GroupPayload
import kotlinx.coroutines.flow.Flow

/**
 * Created by Aditya Gupta on 16/08/23.
 */
interface OfflineDashboardRepository {

    fun allDatabaseClientPayload(): Flow<List<com.mifos.room.entities.client.ClientPayload>>

    fun allDatabaseGroupPayload(): Flow<List<GroupPayload>>

    fun allDatabaseCenterPayload(): Flow<List<CenterPayload>>

    fun databaseLoanRepayments(): Flow<List<LoanRepaymentRequest>>

    fun allSavingsAccountTransactions(): Flow<List<com.mifos.room.entities.accounts.savings.SavingsAccountTransactionRequest>>
}
