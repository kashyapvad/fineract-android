/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServerConfig(
    val protocol: String,
    @SerializedName("end_point")
    val endPoint: String,
    @SerializedName("api_path")
    val apiPath: String,
    val port: String,
    val tenant: String,
) : Parcelable {
    companion object {
        val DEFAULT = try {
            // Try to use the demo server config from BuildConfig if available
            val buildConfigClass = Class.forName("com.mifos.core.common.BuildConfig")
            val demoServerConfigField = buildConfigClass.getDeclaredField("DEMO_SERVER_CONFIG")
            val demoServerConfigValue = demoServerConfigField.get(null) as String
            
            // Parse the JSON string and create ServerConfig
            val jsonString = demoServerConfigValue.replace("'", "\"")
            com.google.gson.Gson().fromJson(jsonString, ServerConfig::class.java)
        } catch (e: Exception) {
            // Fallback to hardcoded default if BuildConfig is not available or parsing fails
            ServerConfig(
                protocol = "https://",
                endPoint = "tt.mifos.community",
                apiPath = "/fineract-provider/api/v1/",
                port = "80",
                tenant = "default",
            )
        }
    }
}

fun ServerConfig.getInstanceUrl(): String {
    val portSuffix = when {
        // Don't include port for standard HTTP/HTTPS ports
        (protocol == "http://" && port == "80") -> ""
        (protocol == "https://" && port == "443") -> ""
        // Don't include port if it's empty or default "80"
        port.isBlank() || port == "80" && protocol == "https://" -> ""
        // Include port for all other cases (like 8443, 8080, etc.)
        else -> ":$port"
    }
    return "$protocol$endPoint$portSuffix$apiPath"
}
