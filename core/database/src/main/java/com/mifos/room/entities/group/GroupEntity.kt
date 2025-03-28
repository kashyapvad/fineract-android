/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.room.entities.group

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.mifos.room.entities.Timeline
import com.mifos.room.entities.client.ClientStatusEntity
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Entity(
    tableName = "GroupTable",
    foreignKeys = [
        ForeignKey(
            entity = GroupDateEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupDate"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
@Serializable
data class GroupEntity(
    @PrimaryKey
    val id: Int? = null,

    val accountNo: String? = null,

    val sync: Boolean = false,

    val name: String? = null,

    val status: ClientStatusEntity? = null,

    val active: Boolean? = null,

    @ColumnInfo(index = true)
    val groupDate: GroupDateEntity? = null,

    val activationDate: List<Int> = emptyList(),

    val officeId: Int? = null,

    val officeName: String? = null,

    val centerId: Int? = 0,

    val centerName: String? = null,

    val staffId: Int? = null,

    val staffName: String? = null,

    val hierarchy: String? = null,

    val groupLevel: Int = 0,

    val timeline: Timeline? = null,

    val externalId: String? = null,
) : Parcelable
