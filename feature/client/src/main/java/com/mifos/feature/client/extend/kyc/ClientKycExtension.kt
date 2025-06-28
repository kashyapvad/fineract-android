/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.client.extend.kyc

import androidx.compose.runtime.Composable
import com.mifos.core.designsystem.component.MifosMenuDropDownItem
import com.mifos.feature.client.extend.ClientMenuExtension
import javax.inject.Inject
import javax.inject.Singleton

/**
 * KYC extension implementation that adds Client KYC and Guarantor KYC menu items
 * This is our custom code that implements the extension interface
 */
@Singleton
class ClientKycExtension @Inject constructor() : ClientMenuExtension {

    @Composable
    override fun MenuItems(
        clientId: Int,
        onMenuClick: (route: String, clientId: Int) -> Unit,
    ) {
        // Client KYC Menu Item
        MifosMenuDropDownItem(
            option = "Client KYC",
            onClick = {
                onMenuClick("client_kyc", clientId)
            },
        )

        // Guarantor KYC Menu Item
        MifosMenuDropDownItem(
            option = "Guarantor KYC",
            onClick = {
                onMenuClick("guarantor_kyc", clientId)
            },
        )
    }
} 
