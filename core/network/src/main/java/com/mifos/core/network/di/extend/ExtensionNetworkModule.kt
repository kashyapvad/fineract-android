/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.network.di.extend

import com.mifos.core.datastore.PrefManager
import com.mifos.core.network.datamanager.extend.DataManagerKyc
import com.mifos.core.databasehelper.extend.DatabaseHelperKyc
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Extension Network DI module that provides network dependencies for KYC and extension features.
 * This keeps extension-related network logic separate from core NetworkModule to minimize upstream changes.
 */
@Module
@InstallIn(SingletonComponent::class)
object ExtensionNetworkModule {

    @Provides
    @Singleton
    fun provideDataManagerKyc(
        baseApiManager: com.mifos.core.network.BaseApiManager,
        databaseHelperKyc: DatabaseHelperKyc,
        prefManager: PrefManager,
    ): DataManagerKyc {
        println("🔧 ExtensionNetworkDI: Creating DataManagerKyc")
        return DataManagerKyc(baseApiManager, databaseHelperKyc, prefManager)
    }
} 