/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */

package com.mifos.feature.client.extend.kyc.ui


import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ContactPage
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

import com.mifos.core.designsystem.component.MifosCircularProgress
import com.mifos.core.designsystem.theme.Black
import com.mifos.core.designsystem.theme.BlueSecondary
import com.mifos.core.designsystem.theme.DarkGray
import com.mifos.core.designsystem.theme.White
import com.mifos.core.objects.client.extend.kyc.ClientKycData
import com.mifos.core.objects.client.extend.kyc.GuarantorKycData
import com.mifos.feature.client.R

/**
 * Composable component to display Client KYC information in client details screen
 * This is placed in extend module to minimize changes to upstream files
 */
@Composable
fun ClientKycDisplay(
    clientId: Int,
    modifier: Modifier = Modifier,
    onNavigateToGuarantorKyc: ((Int) -> Unit)? = null,
    viewModel: ClientKycDisplayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    println("DEBUG: ClientKycDisplay called with clientId: $clientId")
    
    LaunchedEffect(clientId) {
        println("DEBUG: LaunchedEffect triggered for clientId: $clientId")
        viewModel.loadKycData(clientId)
    }
    
    when (val currentState = uiState) {
        is ClientKycDisplayUiState.Loading -> {
            // Show loading indicator
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(BlueSecondary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    MifosCircularProgress()
                }
            }
        }
        
        is ClientKycDisplayUiState.Success -> {
            Column(modifier = modifier) {
                // Client KYC Section
                currentState.clientKyc?.let { clientKyc ->
                    ClientKycCard(
                        title = stringResource(R.string.feature_client_client_kyc),
                        clientKyc = clientKyc
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Guarantor KYC Section  
                if (currentState.guarantorKycList.isNotEmpty()) {
                    GuarantorKycSection(
                        title = stringResource(R.string.feature_client_guarantor_kyc),
                        guarantorKycList = currentState.guarantorKycList,
                        clientId = clientId,
                        onNavigateToGuarantorKyc = onNavigateToGuarantorKyc
                    )
                }
            }
        }
        
        is ClientKycDisplayUiState.Error -> {
            // Silently handle error - KYC is optional information
            // Don't show error state as it would clutter the client details screen
        }
        
        is ClientKycDisplayUiState.Empty -> {
            // Don't show anything if no KYC data exists
        }
    }
}

@Composable
private fun ClientKycCard(
    title: String,
    clientKyc: ClientKycData,
    modifier: Modifier = Modifier
) {
    var expendableState by remember { mutableStateOf(false) }
    val rotateState by animateFloatAsState(
        targetValue = if (expendableState) 180f else 0f,
        label = "client_kyc_arrow_rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(BlueSecondary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    text = title,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal
                    ),
                    color = Black,
                    textAlign = TextAlign.Start
                )
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = { expendableState = !expendableState }
                ) {
                    Icon(
                        modifier = Modifier.rotate(rotateState),
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (expendableState) {
                Spacer(modifier = Modifier.height(10.dp))
                ClientKycContent(clientKyc = clientKyc)
            }
        }
    }
}

@Composable
private fun GuarantorKycCard(
    title: String,
    guarantorKyc: GuarantorKycData,
    modifier: Modifier = Modifier
) {
    var expendableState by remember { mutableStateOf(false) }
    val rotateState by animateFloatAsState(
        targetValue = if (expendableState) 180f else 0f,
        label = "guarantor_kyc_arrow_rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(BlueSecondary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    text = title,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal
                    ),
                    color = Black,
                    textAlign = TextAlign.Start
                )
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = { expendableState = !expendableState }
                ) {
                    Icon(
                        modifier = Modifier.rotate(rotateState),
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (expendableState) {
                Spacer(modifier = Modifier.height(10.dp))
                GuarantorKycContent(guarantorKyc = guarantorKyc)
            }
        }
    }
}

@Composable
private fun GuarantorKycSection(
    title: String,
    guarantorKycList: List<GuarantorKycData>,
    clientId: Int,
    onNavigateToGuarantorKyc: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {

    var expendableState by remember { mutableStateOf(false) }
    val rotateState by animateFloatAsState(
        targetValue = if (expendableState) 180f else 0f,
        label = "guarantor_kyc_section_arrow_rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(BlueSecondary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    text = title,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal
                    ),
                    color = Black,
                    textAlign = TextAlign.Start
                )
                
                // Show count badge if more than 1 guarantor
                if (guarantorKycList.size > 1) {
                    Text(
                        text = "+${guarantorKycList.size - 1}",
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable {
                                onNavigateToGuarantorKyc?.invoke(clientId)
                            },
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = { expendableState = !expendableState }
                ) {
                    Icon(
                        modifier = Modifier.rotate(rotateState),
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (expendableState) {
                Spacer(modifier = Modifier.height(10.dp))
                
                // Show first guarantor details
                GuarantorKycContent(guarantorKyc = guarantorKycList.first())
                
                                 // If more than one guarantor, show a link to view all
                 if (guarantorKycList.size > 1) {
                     Spacer(modifier = Modifier.height(8.dp))
                     
                     Text(
                        text = "View all ${guarantorKycList.size} guarantors →",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onNavigateToGuarantorKyc?.invoke(clientId)
                            }
                            .padding(8.dp),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ClientKycContent(clientKyc: ClientKycData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            clientKyc.panNumber?.let {
                KycDetailRow(
                    icon = Icons.Outlined.Numbers,
                    field = "PAN Number",
                    value = it
                )
            }
            
            clientKyc.aadhaarNumber?.let {
                KycDetailRow(
                    icon = Icons.Outlined.Numbers,
                    field = "Aadhaar Number",
                    value = it
                )
            }
            
            clientKyc.voterIdNumber?.let {
                KycDetailRow(
                    icon = Icons.Outlined.Numbers,
                    field = "Voter ID Number",
                    value = it
                )
            }
            
            clientKyc.drivingLicenseNumber?.let {
                KycDetailRow(
                    icon = Icons.Outlined.Numbers,
                    field = "Driving License Number",
                    value = it
                )
            }
            
            clientKyc.passportNumber?.let {
                KycDetailRow(
                    icon = Icons.Outlined.Numbers,
                    field = "Passport Number",
                    value = it
                )
            }
            
            // Show verification status with method (consistent with Guarantor KYC)
            if (clientKyc.panVerified || clientKyc.aadhaarVerified || clientKyc.aadhaarOtpVerified || 
                clientKyc.voterIdVerified || clientKyc.drivingLicenseVerified || clientKyc.passportVerified) {
                
                KycDetailRow(
                    icon = Icons.Outlined.Badge,
                    field = "Verification Status",
                    value = when {
                        clientKyc.aadhaarOtpVerified -> "Aadhaar verified (OTP)"
                        clientKyc.aadhaarVerified -> "Aadhaar verified (API)"
                        clientKyc.panVerified -> "PAN verified"
                        clientKyc.voterIdVerified -> "Voter ID verified"
                        clientKyc.drivingLicenseVerified -> "Driving License verified"
                        clientKyc.passportVerified -> "Passport verified"
                        else -> "Unverified"
                    }
                )
            }
            
            clientKyc.lastVerifiedOn?.let {
                KycDetailRow(
                    icon = Icons.Outlined.DateRange,
                    field = "Last Verified On",
                    value = it
                )
            }
        }
    }
}

@Composable
private fun GuarantorKycContent(guarantorKyc: GuarantorKycData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            guarantorKyc.fullName?.let {
                KycDetailRow(
                    icon = Icons.Outlined.Person,
                    field = "Guarantor Name",
                    value = it
                )
            }
            
            guarantorKyc.mobileNumber?.let {
                KycDetailRow(
                    icon = Icons.Outlined.ContactPage,
                    field = "Mobile Number",
                    value = it
                )
            }
            
            guarantorKyc.relationshipToClient?.let {
                KycDetailRow(
                    icon = Icons.Outlined.Person,
                    field = "Relationship to Client",
                    value = it
                )
            }
            
            guarantorKyc.panNumber?.let {
                KycDetailRow(
                    icon = Icons.Outlined.Numbers,
                    field = "PAN Number",
                    value = it
                )
            }
            
            guarantorKyc.aadhaarNumber?.let {
                KycDetailRow(
                    icon = Icons.Outlined.Numbers,
                    field = "Aadhaar Number",
                    value = it
                )
            }
            
            // Show verification status with method
            if (guarantorKyc.panVerified || guarantorKyc.aadhaarVerified || guarantorKyc.aadhaarOtpVerified) {
                KycDetailRow(
                    icon = Icons.Outlined.Badge,
                    field = "Verification Status",
                    value = when {
                        guarantorKyc.aadhaarOtpVerified -> "Aadhaar verified (OTP)"
                        guarantorKyc.aadhaarVerified -> "Aadhaar verified (API)"
                        guarantorKyc.panVerified -> "PAN verified"
                        else -> "Unverified"
                    }
                )
            }
            
            guarantorKyc.lastVerifiedOn?.let {
                KycDetailRow(
                    icon = Icons.Outlined.DateRange,
                    field = "Last Verified On",
                    value = it
                )
            }
            
            if (guarantorKyc.isPrimaryGuarantor) {
                KycDetailRow(
                    icon = Icons.Outlined.Badge,
                    field = "Primary Guarantor",
                    value = "Yes"
                )
            }
        }
    }
}

@Composable
private fun KycDetailRow(
    icon: ImageVector,
    field: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = DarkGray
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = field,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Normal
                ),
                color = DarkGray,
                textAlign = TextAlign.Start
            )
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Normal
                ),
                color = Black,
                textAlign = TextAlign.Start
            )
        }
    }
} 