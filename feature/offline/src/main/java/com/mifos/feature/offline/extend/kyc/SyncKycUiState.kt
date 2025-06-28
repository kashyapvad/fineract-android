/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.offline.extend.kyc

import com.mifos.core.objects.client.extend.ClientKycPayload
import com.mifos.core.objects.client.extend.GuarantorKycPayload

/**
 * UI state for KYC sync operations.
 * 
 * Follows the same patterns as other UI state classes in the sync module.
 */
sealed class SyncKycUiState {
    
    data object Loading : SyncKycUiState()
    
    data class Success(
        val clientKycPayloads: List<ClientKycPayload>,
        val guarantorKycPayloads: List<GuarantorKycPayload>,
    ) : SyncKycUiState()
    
    data class Error(val message: String) : SyncKycUiState()
} 