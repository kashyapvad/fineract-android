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

import com.mifos.core.data.repository.SyncCentersDialogRepository
import com.mifos.core.network.datamanager.DataManagerCenter
import com.mifos.core.network.datamanager.DataManagerClient
import com.mifos.core.network.datamanager.DataManagerGroups
import com.mifos.core.network.datamanager.DataManagerLoan
import com.mifos.core.network.datamanager.DataManagerSavings
import com.mifos.room.entities.accounts.CenterAccounts
import com.mifos.room.entities.accounts.ClientAccounts
import com.mifos.room.entities.accounts.GroupAccounts
import com.mifos.room.entities.accounts.loans.LoanWithAssociationsEntity
import com.mifos.room.entities.accounts.savings.SavingsAccountWithAssociationsEntity
import com.mifos.room.entities.client.ClientEntity
import com.mifos.room.entities.group.CenterEntity
import com.mifos.room.entities.group.CenterWithAssociations
import com.mifos.room.entities.group.GroupEntity
import com.mifos.room.entities.group.GroupWithAssociations
import com.mifos.room.entities.templates.loans.LoanRepaymentTemplateEntity
import com.mifos.room.entities.templates.savings.SavingsAccountTransactionTemplateEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by Aditya Gupta on 16/08/23.
 */
class SyncCentersDialogRepositoryImp @Inject constructor(
    private val dataManagerCenter: DataManagerCenter,
    private val dataManagerLoan: DataManagerLoan,
    private val dataManagerSavings: DataManagerSavings,
    private val dataManagerGroups: DataManagerGroups,
    private val dataManagerClient: DataManagerClient,
) : SyncCentersDialogRepository {

    override fun syncCenterAccounts(centerId: Int): Flow<CenterAccounts> {
        return dataManagerCenter.syncCenterAccounts(centerId)
    }

    override fun syncLoanById(loanId: Int): Flow<LoanWithAssociationsEntity> {
        return dataManagerLoan.syncLoanById(loanId)
    }

    override fun syncLoanRepaymentTemplate(loanId: Int): Flow<LoanRepaymentTemplateEntity> {
        return dataManagerLoan.syncLoanRepaymentTemplate(loanId)
    }

    override fun getCenterWithAssociations(centerId: Int): Flow<CenterWithAssociations> {
        return dataManagerCenter.getCenterWithAssociations(centerId)
    }

    override fun getGroupWithAssociations(groupId: Int): Flow<GroupWithAssociations> {
        return dataManagerGroups.getGroupWithAssociations(groupId)
    }

    override fun syncGroupAccounts(groupId: Int): Flow<GroupAccounts> {
        return dataManagerGroups.syncGroupAccounts(groupId)
    }

    override suspend fun syncClientAccounts(clientId: Int): ClientAccounts {
        return dataManagerClient.syncClientAccounts(clientId)
    }

    override suspend fun syncGroupInDatabase(group: GroupEntity) {
        dataManagerGroups.syncGroupInDatabase(group)
    }

    override suspend fun syncClientInDatabase(client: ClientEntity) {
        dataManagerClient.syncClientInDatabase(client)
    }

    override suspend fun syncCenterInDatabase(center: CenterEntity) {
        dataManagerCenter.syncCenterInDatabase(center)
    }

    override fun syncSavingsAccount(
        type: String?,
        savingsAccountId: Int,
        association: String?,
    ): Flow<SavingsAccountWithAssociationsEntity> {
        return dataManagerSavings.syncSavingsAccount(type, savingsAccountId, association)
    }

    override fun syncSavingsAccountTransactionTemplate(
        savingsAccountType: String?,
        savingsAccountId: Int,
        transactionType: String?,
    ): Flow<SavingsAccountTransactionTemplateEntity> {
        return dataManagerSavings.syncSavingsAccountTransactionTemplate(
            savingsAccountType,
            savingsAccountId,
            transactionType,
        )
    }
}
