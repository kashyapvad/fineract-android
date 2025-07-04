/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.domain.useCases

import com.mifos.core.common.utils.Resource
import com.mifos.core.data.repository.ClientIdentifierDialogRepository
import com.mifos.core.objects.noncore.IdentifierCreationResponse
import com.mifos.core.objects.noncore.IdentifierPayload
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateClientIdentifierUseCase @Inject constructor(
    private val repository: ClientIdentifierDialogRepository,
) {

    operator fun invoke(
        clientId: Int,
        identifierPayload: IdentifierPayload,
    ): Flow<Resource<IdentifierCreationResponse>> =
        repository.createClientIdentifier(clientId, identifierPayload)
}
