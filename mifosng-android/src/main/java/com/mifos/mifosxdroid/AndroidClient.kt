/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.mifosxdroid

import android.content.Context
import android.graphics.Typeface
import android.os.StrictMode

import androidx.multidex.MultiDexApplication
import com.mifos.core.common.utils.LanguageHelper
import com.mifos.core.data.services.extend.ExtensionInitializer
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Created by ishankhanna on 13/03/15.
 */
@HiltAndroidApp
class AndroidClient : MultiDexApplication() {
    
    @Inject
    lateinit var extensionInitializer: ExtensionInitializer
    
    private var extensionsInitialized = false
    
    override fun onCreate() {
        super.onCreate()

        println("🚀 AndroidClient: onCreate started")
        instance = this
        // Initializing the DBFlow and SQL Cipher Encryption
        FlowManager.init(FlowConfig.Builder(this).build())
        val policy = StrictMode.VmPolicy.Builder()
            .detectFileUriExposure()
            .build()
        StrictMode.setVmPolicy(policy)
        
        println("🚀 AndroidClient: onCreate completed - extensions will be initialized lazily")
    }
    
    /**
     * Initialize extensions when first needed (after DI is ready)
     */
    fun ensureExtensionsInitialized() {
        if (!extensionsInitialized) {
            println("🚀 AndroidClient: Initializing extension system lazily...")
            try {
                extensionInitializer.initialize()
                extensionsInitialized = true
                println("✅ AndroidClient: Extension system initialized successfully")
            } catch (e: Exception) {
                println("❌ AndroidClient: Extension system initialization failed - ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LanguageHelper.onAttach(base))
    }

    companion object {
        @JvmField
        val typefaceManager: MutableMap<Int, Typeface?> = HashMap()

        @JvmStatic
        var instance: AndroidClient? = null

        @JvmStatic
        val context: AndroidClient?
            get() = instance

        @JvmStatic
        operator fun get(context: Context): AndroidClient {
            return context.applicationContext as AndroidClient
        }
    }
}
