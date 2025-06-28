/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.client.extend.preSyncValidation

import com.mifos.core.objects.client.ClientPayload
import com.mifos.core.objects.templates.clients.ClientsTemplate

/**
 * UI State for Pre-Sync Validation Screen
 */
sealed class PreSyncValidationUiState {
    data object Loading : PreSyncValidationUiState()
    
    data class ShowInvalidPayloads(
        val invalidPayloads: List<ClientPayload>,
        val clientTemplate: ClientsTemplate?,
    ) : PreSyncValidationUiState()
    
    data class Error(val message: String) : PreSyncValidationUiState()
} 