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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mifos.core.designsystem.component.MifosCircularProgress
import com.mifos.core.designsystem.component.MifosScaffold
import com.mifos.core.designsystem.icon.MifosIcons
import com.mifos.core.objects.client.Client
import com.mifos.core.objects.client.extend.kyc.ClientKycData
import com.mifos.feature.client.R

/**
 * Client KYC Screen for creating and editing KYC documents
 * Simplified offline-first implementation focused on document creation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientKycScreen(
    clientId: Int,
    isOffline: Boolean = false,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientKycViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(clientId, isOffline) {
        viewModel.initializeKyc(clientId, isOffline)
    }

    MifosScaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.feature_client_client_kyc)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = MifosIcons.arrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (val currentState = uiState) {
                is ClientKycUiState.Loading -> {
                    MifosCircularProgress()
                }
                is ClientKycUiState.Error -> {
                    ErrorState(
                        message = currentState.message,
                        onRetry = { viewModel.initializeKyc(clientId) },
                    )
                }
                is ClientKycUiState.FormReady -> {
                    ClientKycForm(
                        uiState = currentState,
                        onFormDataChange = viewModel::updateFormData,
                        onSubmitKyc = viewModel::submitKyc,
                        onEditKyc = { viewModel.enableEditMode() },
                        onVerifyKyc = { viewModel.initiateOtpVerification() },
                    )
                }
                is ClientKycUiState.KycExists -> {
                    ExistingKycView(
                        kycData = currentState.kycData,
                        client = currentState.client,
                        onEditKyc = { viewModel.enableEditMode() },
                        onVerifyKyc = { viewModel.initiateOtpVerification() },
                        onClose = onBackPressed,
                    )
                }
                is ClientKycUiState.OtpVerificationForm -> {
                    OtpVerificationView(
                        kycData = currentState.kycData,
                        client = currentState.client,
                        isGeneratingOtp = currentState.isGeneratingOtp,
                        otpGenerated = currentState.otpGenerated,
                        isSubmittingOtp = currentState.isSubmittingOtp,
                        onGenerateOtp = { viewModel.generateOtp() },
                        onSubmitOtp = { otp, notes -> viewModel.submitOtp(otp, notes) },
                        onCancel = { viewModel.cancelOtpVerification() },
                    )
                }
                is ClientKycUiState.Success -> {
                    SuccessState(
                        message = currentState.message,
                        onClose = onBackPressed,
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Error: $message",
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun SuccessState(
    message: String,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onClose) {
            Text("Close")
        }
    }
}



@Composable
private fun ClientKycForm(
    uiState: ClientKycUiState.FormReady,
    onFormDataChange: (ClientKycFormData) -> Unit,
    onSubmitKyc: (ClientKycFormData) -> Unit,
    onEditKyc: () -> Unit,
    onVerifyKyc: () -> Unit,
) {
    var formData by remember { mutableStateOf(uiState.formData) }

    // Update form data when it changes
    LaunchedEffect(formData) {
        onFormDataChange(formData)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Client Info Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Client Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${uiState.client.displayName ?: "Unknown"}")
                Text("Account Number: ${uiState.client.accountNo ?: "N/A"}")
                Text("Status: ${if (uiState.client.active == true) "Active" else "Inactive"}")
                if (uiState.syncStatus != null) {
                    Text(
                        text = "Sync Status: ${uiState.syncStatus}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }

        // KYC Form Fields
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "KYC Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                OutlinedTextField(
                    value = formData.panNumber,
                    onValueChange = { formData = formData.copy(panNumber = it) },
                    label = { Text("PAN Number") },
                    placeholder = { Text("ABCDE1234F") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isPanNumberValid(formData.panNumber) && formData.panNumber.isNotBlank(),
                    supportingText = {
                        if (!isPanNumberValid(formData.panNumber) && formData.panNumber.isNotBlank()) {
                            Text(
                                text = "Invalid PAN format (e.g., ABCDE1234F)",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = formData.aadhaarNumber,
                    onValueChange = { formData = formData.copy(aadhaarNumber = it) },
                    label = { Text("Aadhaar Number") },
                    placeholder = { Text("123456789012") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !isAadhaarNumberValid(formData.aadhaarNumber) && formData.aadhaarNumber.isNotBlank(),
                    supportingText = {
                        if (!isAadhaarNumberValid(formData.aadhaarNumber) && formData.aadhaarNumber.isNotBlank()) {
                            Text(
                                text = "Invalid Aadhaar format (12 digits)",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = formData.drivingLicenseNumber,
                    onValueChange = { 
                        if (it.length <= 20) formData = formData.copy(drivingLicenseNumber = it) 
                    },
                    label = { Text("Driving License Number") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text("${formData.drivingLicenseNumber.length}/20")
                    }
                )

                OutlinedTextField(
                    value = formData.voterId,
                    onValueChange = { 
                        if (it.length <= 20) formData = formData.copy(voterId = it) 
                    },
                    label = { Text("Voter ID") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text("${formData.voterId.length}/20")
                    }
                )

                OutlinedTextField(
                    value = formData.passportNumber,
                    onValueChange = { 
                        if (it.length <= 20) formData = formData.copy(passportNumber = it) 
                    },
                    label = { Text("Passport Number") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text("${formData.passportNumber.length}/20")
                    }
                )

                OutlinedTextField(
                    value = formData.verificationNotes,
                    onValueChange = { formData = formData.copy(verificationNotes = it) },
                    label = { Text("Verification Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    supportingText = {
                        Text("Optional notes for verification process")
                    }
                )

                // Submit Button
                Button(
                    onClick = { onSubmitKyc(formData) },
                    enabled = !uiState.isLoading && isFormValid(formData),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (uiState.isLoading) "Processing..." else if (uiState.isEditMode) "Update KYC" else "Submit KYC",
                    )
                }
            }
        }
    }
}

@Composable
private fun ExistingKycView(
    kycData: ClientKycData,
    client: Client,
    onEditKyc: () -> Unit,
    onVerifyKyc: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Client Info Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Client Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${client.displayName ?: "Unknown"}")
                Text("Account Number: ${client.accountNo ?: "N/A"}")
                Text("Status: ${if (client.active == true) "Active" else "Inactive"}")
                Text(
                    text = "KYC Status: Already Added",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // Offline Sync Status Card (only show if pending sync)
        if (kycData.offlineData && kycData.syncStatus?.startsWith("PENDING") == true) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📤 Pending Sync",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "KYC changes will be synced when you go online",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Status: ${kycData.syncStatus}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }

        // KYC Details Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "KYC Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Create local immutable references to avoid smart cast issues
                val panNumber = kycData.panNumber
                val aadhaarNumber = kycData.aadhaarNumber
                val drivingLicenseNumber = kycData.drivingLicenseNumber
                val voterIdNumber = kycData.voterIdNumber
                val passportNumber = kycData.passportNumber
                val manualVerificationNotes = kycData.manualVerificationNotes
                
                if (!panNumber.isNullOrEmpty()) {
                    KycDetailRow("PAN Number", panNumber, kycData.panVerified)
                }
                if (!aadhaarNumber.isNullOrEmpty()) {
                    KycDetailRow("Aadhaar Number", aadhaarNumber, kycData.aadhaarVerified || kycData.aadhaarOtpVerified, 
                        verificationMethod = when {
                            kycData.aadhaarOtpVerified -> "OTP"
                            kycData.aadhaarVerified -> "API"
                            else -> null
                        })
                }
                if (!drivingLicenseNumber.isNullOrEmpty()) {
                    KycDetailRow("Driving License", drivingLicenseNumber, kycData.drivingLicenseVerified)
                }
                if (!voterIdNumber.isNullOrEmpty()) {
                    KycDetailRow("Voter ID", voterIdNumber, kycData.voterIdVerified)
                }
                if (!passportNumber.isNullOrEmpty()) {
                    KycDetailRow("Passport Number", passportNumber, kycData.passportVerified)
                }
                if (!manualVerificationNotes.isNullOrEmpty()) {
                    Text(
                        text = "Verification Notes:",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = manualVerificationNotes,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onEditKyc,
                modifier = Modifier.weight(1f),
            ) {
                Text("Edit KYC")
            }
            
            Button(
                onClick = onVerifyKyc,
                modifier = Modifier.weight(1f),
            ) {
                Text("Verify via OTP")
            }
        }
        
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Close")
        }
    }
}

@Composable
private fun KycDetailRow(
    label: String,
    value: String,
    isVerified: Boolean?,
    verificationMethod: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        if (isVerified != null) {
            Text(
                text = when {
                    isVerified && verificationMethod != null -> "✓ Verified ($verificationMethod)"
                    isVerified -> "✓ Verified"
                    else -> "⚠ Unverified"
                },
                fontSize = 12.sp,
                color = if (isVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun isFormValid(formData: ClientKycFormData): Boolean {
    return formData.panNumber.isNotBlank() ||
        formData.aadhaarNumber.isNotBlank() ||
        formData.drivingLicenseNumber.isNotBlank() ||
        formData.voterId.isNotBlank() ||
        formData.passportNumber.isNotBlank()
}

private fun isPanNumberValid(panNumber: String): Boolean {
    // PAN format: 5 uppercase letters, 4 digits, 1 uppercase letter (e.g., ABCDE1234F)
    val panPattern = "^[A-Z]{5}[0-9]{4}[A-Z]$".toRegex()
    return panNumber.isEmpty() || panPattern.matches(panNumber)
}

private fun isAadhaarNumberValid(aadhaarNumber: String): Boolean {
    // Aadhaar format: exactly 12 digits
    val aadhaarPattern = "^[0-9]{12}$".toRegex()
    return aadhaarNumber.isEmpty() || aadhaarPattern.matches(aadhaarNumber)
}

@Composable
private fun OtpVerificationView(
    kycData: ClientKycData,
    client: Client,
    isGeneratingOtp: Boolean,
    otpGenerated: Boolean,
    isSubmittingOtp: Boolean,
    onGenerateOtp: () -> Unit,
    onSubmitOtp: (String, String) -> Unit,
    onCancel: () -> Unit,
) {
    var otp by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Client Info Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Client Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${client.displayName ?: "Unknown"}")
                Text("Account Number: ${client.accountNo ?: "N/A"}")
                Text("Status: ${if (client.active == true) "Active" else "Inactive"}")
            }
        }

        // OTP Verification Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Aadhaar OTP Verification",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Aadhaar Number: ${kycData.aadhaarNumber ?: "N/A"}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!otpGenerated) {
                    Text(
                        text = "An OTP will be sent to your registered mobile number linked with Aadhaar.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onGenerateOtp,
                        enabled = !isGeneratingOtp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isGeneratingOtp) "Generating OTP..." else "Generate OTP")
                    }
                } else {
                    Text(
                        text = "OTP has been sent to your registered mobile number. Please enter it below:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) otp = it },
                        label = { Text("Enter OTP") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        supportingText = {
                            Text("${otp.length}/6 digits")
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Verification Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { onSubmitOtp(otp, notes) },
                        enabled = !isSubmittingOtp && otp.length == 6,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isSubmittingOtp) "Verifying..." else "Verify OTP")
                    }
                }
            }
        }
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            ) {
                Text("Cancel")
            }
            
            if (otpGenerated) {
                Button(
                    onClick = onGenerateOtp,
                    enabled = !isGeneratingOtp,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Resend OTP")
                }
            }
        }
    }
} 
