/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.client.extend.kyc.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mifos.core.common.utils.Constants
import com.mifos.feature.client.extend.kyc.ui.ClientKycScreen
import com.mifos.feature.client.extend.kyc.ui.GuarantorKycScreen
import com.mifos.feature.client.extend.kyc.ui.OfflineClientOptionsScreen

object KycScreens {
    const val CLIENT_KYC_ROUTE = "client_kyc_screen/{${Constants.CLIENT_ID}}/{isOffline}"
    const val GUARANTOR_KYC_ROUTE = "guarantor_kyc_screen/{${Constants.CLIENT_ID}}/{isOffline}"
    const val OFFLINE_CLIENT_OPTIONS_ROUTE = "offline_client_options/{${Constants.CLIENT_ID}}"

    fun clientKycRoute(clientId: Int, isOffline: Boolean = false) = "client_kyc_screen/$clientId/$isOffline"
    fun guarantorKycRoute(clientId: Int, isOffline: Boolean = false) = "guarantor_kyc_screen/$clientId/$isOffline"
    fun offlineClientOptionsRoute(clientId: Int) = "offline_client_options/$clientId"
}

fun NavGraphBuilder.addKycRoutes(
    navController: NavController,
    onBackPressed: () -> Unit
) {
    composable(
        route = KycScreens.CLIENT_KYC_ROUTE,
        arguments = listOf(
            navArgument(Constants.CLIENT_ID) { type = NavType.IntType },
            navArgument("isOffline") { type = NavType.BoolType; defaultValue = false }
        ),
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getInt(Constants.CLIENT_ID) ?: 0
        val isOffline = backStackEntry.arguments?.getBoolean("isOffline") ?: false
        ClientKycScreen(clientId = clientId, isOffline = isOffline, onBackPressed = onBackPressed)
    }

    composable(
        route = KycScreens.GUARANTOR_KYC_ROUTE,
        arguments = listOf(
            navArgument(Constants.CLIENT_ID) { type = NavType.IntType },
            navArgument("isOffline") { type = NavType.BoolType; defaultValue = false }
        ),
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getInt(Constants.CLIENT_ID) ?: 0
        val isOffline = backStackEntry.arguments?.getBoolean("isOffline") ?: false
        GuarantorKycScreen(clientId = clientId, isOffline = isOffline, onBackPressed = onBackPressed)
    }

    composable(
        route = KycScreens.OFFLINE_CLIENT_OPTIONS_ROUTE,
        arguments = listOf(navArgument(Constants.CLIENT_ID) { type = NavType.IntType }),
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getInt(Constants.CLIENT_ID) ?: 0
        OfflineClientOptionsScreen(
            clientPayloadId = clientId,
            onBackPressed = onBackPressed,
            onNavigateToClientKyc = { payloadId ->
                // Navigate to Client KYC with offline flag
                navController.navigateToClientKyc(payloadId, isOffline = true)
            },
            onNavigateToGuarantorKyc = { payloadId ->
                // Navigate to Guarantor KYC with offline flag  
                navController.navigateToGuarantorKyc(payloadId, isOffline = true)
            }
        )
    }
}

fun NavController.navigateToClientKyc(clientId: Int, isOffline: Boolean = false) {
    navigate(KycScreens.clientKycRoute(clientId, isOffline))
}

fun NavController.navigateToGuarantorKyc(clientId: Int, isOffline: Boolean = false) {
    navigate(KycScreens.guarantorKycRoute(clientId, isOffline))
}

fun NavController.navigateToOfflineClientOptions(clientId: Int) {
    navigate(KycScreens.offlineClientOptionsRoute(clientId))
}

fun isKycRoute(route: String): Boolean {
    return when (route) {
        "client_kyc", "guarantor_kyc" -> true
        else -> false
    }
} 
