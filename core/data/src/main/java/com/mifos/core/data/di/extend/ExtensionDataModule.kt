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


import com.mifos.core.data.services.extend.ClientCreationHelper
import com.mifos.core.data.services.extend.ClientKycSyncService
import com.mifos.core.data.services.extend.ExtensionInitializer
import com.mifos.core.data.services.extend.KycSyncEventListener
import com.mifos.core.data.repository.extend.SyncKycRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Extension DI module that provides dependencies for KYC sync event system.
 * This keeps extension logic separate from core DataModule to minimize upstream changes.
 */
@Module
@InstallIn(SingletonComponent::class)
object ExtensionDataModule {

    @Provides
    @Singleton
    fun provideExtensionInitializer(kycSyncEventListener: KycSyncEventListener): ExtensionInitializer {
        println("🔧 ExtensionDI: Creating ExtensionInitializer")
        return ExtensionInitializer(kycSyncEventListener)
    }

    @Provides
    @Singleton
    fun provideClientCreationHelper(extensionInitializer: ExtensionInitializer): ClientCreationHelper {
        println("🔧 ExtensionDI: Creating ClientCreationHelper")
        return ClientCreationHelper(extensionInitializer)
    }

    @Provides
    @Singleton
    fun provideKycSyncEventListener(clientKycSyncService: ClientKycSyncService): KycSyncEventListener {
        println("🔧 ExtensionDI: Creating KycSyncEventListener")
        return KycSyncEventListener(clientKycSyncService)
    }

    @Provides
    @Singleton
    fun provideClientKycSyncService(syncKycRepository: SyncKycRepository): ClientKycSyncService {
        println("🔧 ExtensionDI: Creating ClientKycSyncService")
        return ClientKycSyncService(syncKycRepository)
    }
} 