/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.room.entities.survey

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(tableName = "Survey")
data class SurveyEntity(
    @PrimaryKey
    val id: Int = 0,

    val key: String? = null,

    val name: String? = null,

    val description: String? = null,

    val isSync: Boolean = false,

    val countryCode: String? = null,

    val questionDatas: List<QuestionDatasEntity> = emptyList(),

    val componentDatas: List<ComponentDatasEntity> = emptyList(),
) : Parcelable
