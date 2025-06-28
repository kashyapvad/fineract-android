/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.network.adapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Custom deserializer to handle date arrays from server response.
 * 
 * The server returns dates as arrays like [2025,6,27,7,8,9] but 
 * the client expects them as strings. This deserializer converts
 * the arrays to formatted date strings.
 */
class DateArrayDeserializer : JsonDeserializer<String> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): String? {
        return when {
            json == null || json.isJsonNull -> null
            json.isJsonPrimitive -> json.asString
            json.isJsonArray -> {
                val dateArray = json.asJsonArray
                if (dateArray.size() >= 3) {
                    // Convert [2025,6,27,7,8,9] to "2025-06-27 07:08:09" format
                    val year = dateArray[0].asInt
                    val month = dateArray[1].asInt
                    val day = dateArray[2].asInt
                    
                    if (dateArray.size() >= 6) {
                        val hour = dateArray[3].asInt
                        val minute = dateArray[4].asInt
                        val second = dateArray[5].asInt
                        String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second)
                    } else {
                        String.format("%04d-%02d-%02d", year, month, day)
                    }
                } else null
            }
            else -> null
        }
    }
} 