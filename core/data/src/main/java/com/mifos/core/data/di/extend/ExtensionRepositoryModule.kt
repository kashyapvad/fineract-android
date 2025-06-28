/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.data.di.extend


import com.mifos.core.data.repository.extend.ClientKycRepository
import com.mifos.core.data.repository.extend.GuarantorKycRepository
import com.mifos.core.data.repository.extend.SyncKycRepository
import com.mifos.core.data.repositoryImp.extend.ClientKycRepositoryImp
import com.mifos.core.data.repositoryImp.extend.GuarantorKycRepositoryImp
import com.mifos.core.data.repositoryImp.extend.SyncKycRepositoryImp
import com.mifos.core.databasehelper.extend.DatabaseHelperKyc
import com.mifos.core.network.datamanager.extend.DataManagerKyc
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Extension repository DI module for KYC-related repositories.
 * This keeps extension repository logic separate from core RepositoryModule.
 */
@Module
@InstallIn(SingletonComponent::class)
object ExtensionRepositoryModule {

    @Provides
    @Singleton
    fun providesSyncKycRepository(
        databaseHelperKyc: DatabaseHelperKyc,
        dataManagerKyc: DataManagerKyc,
    ): SyncKycRepository {
        println("🔧 ExtensionDI: Creating SyncKycRepository")
        return SyncKycRepositoryImp(databaseHelperKyc, dataManagerKyc)
    }

    @Provides
    fun providesClientKycRepository(dataManagerKyc: DataManagerKyc): ClientKycRepository {
        println("🔧 ExtensionDI: Creating ClientKycRepository")
        return ClientKycRepositoryImp(dataManagerKyc)
    }

    @Provides
    fun providesGuarantorKycRepository(dataManagerKyc: DataManagerKyc): GuarantorKycRepository {
        println("🔧 ExtensionDI: Creating GuarantorKycRepository")
        return GuarantorKycRepositoryImp(dataManagerKyc)
    }
} 