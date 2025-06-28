/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.objects.client.extend

import android.os.Parcelable
import com.mifos.core.database.MifosDatabase
import com.mifos.core.model.MifosBaseModel
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ModelContainer
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import kotlinx.parcelize.Parcelize

/**
 * Database entity for Client KYC payload storage in offline mode.
 * 
 * This stores KYC data created offline that needs to be synced when network becomes available.
 * Follows the same pattern as ClientPayload with proper dependency tracking.
 */
@Parcelize
@Table(database = MifosDatabase::class, useBooleanGetterSetters = false)
@ModelContainer
data class ClientKycPayload(
    @PrimaryKey(autoincrement = true)
    @Transient
    var id: Int? = null,

    @Column
    @Transient
    var errorMessage: String? = null,

    // Client reference - can be local client ID for offline-created clients
    @Column
    var clientId: Int? = null,

    // For dependency tracking when client is also created offline
    @Column
    @Transient
    var clientCreationTime: Long? = null,

    // Sync status tracking
    @Column
    @Transient
    var syncStatus: String = "PENDING_CREATE", // PENDING_CREATE, PENDING_UPDATE, SYNCED, ERROR

    // Creation timestamp for ordering and dependency resolution
    @Column
    @Transient
    var kycCreationTime: Long? = null,

    // KYC Document Fields
    @Column
    var panNumber: String? = null,

    @Column
    var aadhaarNumber: String? = null,

    @Column
    var voterId: String? = null,

    @Column
    var drivingLicenseNumber: String? = null,

    @Column
    var passportNumber: String? = null,

    @Column
    var verificationNotes: String? = null,

    // Server-side KYC ID after successful sync
    @Column
    @Transient
    var serverKycId: Long? = null,

    // Standard fields
    @Column
    var dateFormat: String? = "dd MMMM yyyy",

    @Column
    var locale: String? = "en",
) : MifosBaseModel(), Parcelable 