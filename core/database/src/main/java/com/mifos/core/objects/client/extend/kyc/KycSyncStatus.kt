/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.objects.client.extend.kyc

/**
 * Offline sync status for KYC records.
 */
enum class KycSyncStatus {
    SYNCED, // Fully synced with server
    PENDING_CREATE, // Needs to be created on server
    PENDING_UPDATE, // Needs to be updated on server
    PENDING_OTP, // Form data synced, OTP verification pending
    SYNC_ERROR, // Sync failed, needs retry
} 