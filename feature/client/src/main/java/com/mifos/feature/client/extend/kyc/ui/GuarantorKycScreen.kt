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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mifos.core.designsystem.component.MifosCircularProgress
import com.mifos.core.designsystem.icon.MifosIcons
import com.mifos.core.objects.client.extend.kyc.GuarantorKycData
import com.mifos.core.objects.client.extend.kyc.RelationshipType

/**
 * Guarantor KYC Screen with OTP verification flow
 * Similar to Client KYC but for guarantor verification
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuarantorKycScreen(
    clientId: Int,
    isOffline: Boolean = false,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GuarantorKycViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(clientId, isOffline) {
        viewModel.loadClientDetails(clientId, isOffline)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Guarantor KYC") },
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
                is GuarantorKycUiState.Loading -> {
                    MifosCircularProgress()
                }
                is GuarantorKycUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Error: ${currentState.message}",
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadClientDetails(clientId) }) {
                            Text("Retry")
                        }
                    }
                }
                is GuarantorKycUiState.GuarantorsList -> {
                    GuarantorsListView(
                        client = currentState.client,
                        guarantors = currentState.guarantors,
                        onAddGuarantor = { viewModel.showAddForm() },
                        onEditGuarantor = { viewModel.editGuarantor(it) },
                        onVerifyGuarantor = { viewModel.initiateOtpVerification(it) },
                        onDeleteGuarantor = { viewModel.deleteGuarantor(it) },
                    )
                }
                is GuarantorKycUiState.FormReady -> {
                    GuarantorKycForm(
                        client = currentState.client,
                        formData = currentState.formData,
                        isEditMode = currentState.isEditMode,
                        onFormDataChange = viewModel::updateFormData,
                        onSubmit = viewModel::submitGuarantorKyc,
                        onCancel = { viewModel.loadClientDetails(clientId) },
                    )
                }
                is GuarantorKycUiState.OtpVerificationForm -> {
                    OtpVerificationView(
                        guarantor = currentState.guarantor,
                        client = currentState.client,
                        isGeneratingOtp = currentState.isGeneratingOtp,
                        otpGenerated = currentState.otpGenerated,
                        isSubmittingOtp = currentState.isSubmittingOtp,
                        onGenerateOtp = { viewModel.generateOtp() },
                        onSubmitOtp = { otp, notes -> viewModel.submitOtp(otp, notes) },
                        onCancel = { viewModel.cancelOtpVerification() },
                    )
                }
                is GuarantorKycUiState.Success -> {
                    // Auto-reload the guarantor list after a short delay
                    LaunchedEffect(currentState) {
                        kotlinx.coroutines.delay(1500) // Show success message for 1.5 seconds
                        viewModel.loadClientDetails(currentState.client.id)
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Success!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentState.message,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Returning to guarantor list...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.loadClientDetails(currentState.client.id) }
                            ) {
                                Text("View List")
                            }
                            Button(
                                onClick = onBackPressed
                            ) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuarantorsListView(
    client: com.mifos.core.objects.client.Client,
    guarantors: List<GuarantorKycData>,
    onAddGuarantor: () -> Unit,
    onEditGuarantor: (GuarantorKycData) -> Unit,
    onVerifyGuarantor: (GuarantorKycData) -> Unit,
    onDeleteGuarantor: (GuarantorKycData) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Client Information Card
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

        // Guarantors Header with Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Guarantors (${guarantors.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Button(onClick = onAddGuarantor) {
                Text("+ Guarantor")
            }
        }

        if (guarantors.isEmpty()) {
            // Empty state
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No guarantors added yet",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add guarantor KYC details to get started",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        } else {
            // Guarantors List
            guarantors.forEach { guarantor ->
                GuarantorKycCard(
                    guarantor = guarantor,
                    onEdit = { onEditGuarantor(guarantor) },
                    onVerify = { onVerifyGuarantor(guarantor) },
                    onDelete = { onDeleteGuarantor(guarantor) },
                )
            }
        }
    }
}

@Composable
private fun GuarantorKycCard(
    guarantor: GuarantorKycData,
    onEdit: () -> Unit,
    onVerify: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                            Text(
                text = guarantor.fullName ?: "Unknown",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
                if (guarantor.isPrimaryGuarantor) {
                    Text(
                        text = "PRIMARY",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Mobile: ${guarantor.mobileNumber ?: "N/A"}")
            Text("Relationship: ${guarantor.relationshipToClient ?: "N/A"}")
            
            if (!guarantor.panNumber.isNullOrEmpty()) {
                Text("PAN: ${guarantor.panNumber}")
            }
            if (!guarantor.aadhaarNumber.isNullOrEmpty()) {
                Text("Aadhaar: ${guarantor.aadhaarNumber}")
            }
            
            // Verification Status
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Verification: ")
                Text(
                    text = when {
                        guarantor.aadhaarOtpVerified -> "✓ OTP Verified"
                        guarantor.aadhaarVerified -> "✓ API Verified"
                        guarantor.panVerified -> "✓ PAN Verified"
                        else -> "⚠ Unverified"
                    },
                    color = when {
                        guarantor.aadhaarOtpVerified || guarantor.aadhaarVerified || guarantor.panVerified -> 
                            MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.error
                    },
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Edit")
                }
                
                Button(
                    onClick = onVerify,
                    modifier = Modifier.weight(1.2f),
                ) {
                    Text(
                        text = "Verify OTP",
                        fontSize = 12.sp
                    )
                }
                
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuarantorKycForm(
    client: com.mifos.core.objects.client.Client,
    formData: GuarantorKycFormData,
    isEditMode: Boolean = false,
    onFormDataChange: (GuarantorKycFormData) -> Unit,
    onSubmit: (GuarantorKycFormData) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var relationshipExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Client Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
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

        // Guarantor Information Form
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Guarantor Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                // Full Name
                OutlinedTextField(
                    value = formData.fullName,
                    onValueChange = { onFormDataChange(formData.copy(fullName = it)) },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formData.fullName.isBlank(),
                    supportingText = if (formData.fullName.isBlank()) {
                        { Text("Full name is required") }
                    } else null,
                )

                // Mobile Number
                OutlinedTextField(
                    value = formData.mobileNumber,
                    onValueChange = { onFormDataChange(formData.copy(mobileNumber = it)) },
                    label = { Text("Mobile Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = formData.mobileNumber.isBlank() || !isValidMobileNumber(formData.mobileNumber),
                    supportingText = if (formData.mobileNumber.isBlank()) {
                        { Text("Mobile number is required") }
                    } else if (!isValidMobileNumber(formData.mobileNumber)) {
                        { Text("Please enter a valid 10-digit mobile number") }
                    } else null,
                )

                // Relationship Dropdown
                ExposedDropdownMenuBox(
                    expanded = relationshipExpanded,
                    onExpandedChange = { relationshipExpanded = !relationshipExpanded },
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        readOnly = true,
                        value = RelationshipType.getDisplayName(formData.relationshipToClient),
                        onValueChange = { },
                        label = { Text("Relationship to Client") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = relationshipExpanded,
                            )
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    )
                    ExposedDropdownMenu(
                        expanded = relationshipExpanded,
                        onDismissRequest = { relationshipExpanded = false },
                    ) {
                        formData.availableRelationships.forEach { relationship ->
                            DropdownMenuItem(
                                text = { Text(RelationshipType.getDisplayName(relationship)) },
                                onClick = {
                                    onFormDataChange(formData.copy(relationshipToClient = relationship))
                                    relationshipExpanded = false
                                },
                            )
                        }
                    }
                }

                // PAN Number
                OutlinedTextField(
                    value = formData.panNumber,
                    onValueChange = { onFormDataChange(formData.copy(panNumber = it.uppercase())) },
                    label = { Text("PAN Number") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formData.panNumber.isNotBlank() && !isValidPanNumber(formData.panNumber),
                    supportingText = if (formData.panNumber.isNotBlank() && !isValidPanNumber(formData.panNumber)) {
                        { Text("Please enter a valid PAN number (e.g., ABCDE1234F)") }
                    } else null,
                )

                // Aadhaar Number
                OutlinedTextField(
                    value = formData.aadhaarNumber,
                    onValueChange = { input ->
                        // Only allow digits and limit to 12 characters
                        val digits = input.filter { it.isDigit() }.take(12)
                        onFormDataChange(formData.copy(aadhaarNumber = digits))
                    },
                    label = { Text("Aadhaar Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = formData.aadhaarNumber.isNotBlank() && !isValidAadhaarNumber(formData.aadhaarNumber),
                    supportingText = if (formData.aadhaarNumber.isNotBlank() && !isValidAadhaarNumber(formData.aadhaarNumber)) {
                        { Text("Please enter a valid 12-digit Aadhaar number") }
                    } else null,
                )

                // Status Checkboxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = formData.isPrimaryGuarantor,
                        onCheckedChange = { onFormDataChange(formData.copy(isPrimaryGuarantor = it)) },
                    )
                    Text("Primary Guarantor")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = formData.isActive,
                        onCheckedChange = { onFormDataChange(formData.copy(isActive = it)) },
                    )
                    Text("Active")
                }

                // Verification Notes
                OutlinedTextField(
                    value = formData.verificationNotes,
                    onValueChange = { onFormDataChange(formData.copy(verificationNotes = it)) },
                    label = { Text("Verification Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                )

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
                    
                    Button(
                        onClick = { onSubmit(formData) },
                        enabled = isFormValid(formData),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (isEditMode) "Update" else "+ Guarantor")
                    }
                }
            }
        }
    }
}

// Validation Functions
private fun isFormValid(formData: GuarantorKycFormData): Boolean {
    return formData.fullName.isNotBlank() &&
        formData.mobileNumber.isNotBlank() &&
        isValidMobileNumber(formData.mobileNumber) &&
        (formData.panNumber.isBlank() || isValidPanNumber(formData.panNumber)) &&
        (formData.aadhaarNumber.isBlank() || isValidAadhaarNumber(formData.aadhaarNumber))
}

private fun isValidMobileNumber(mobile: String): Boolean {
    return mobile.length == 10 && mobile.all { it.isDigit() }
}

private fun isValidPanNumber(pan: String): Boolean {
    val panRegex = "^[A-Z]{5}[0-9]{4}[A-Z]$".toRegex()
    return panRegex.matches(pan)
}

private fun isValidAadhaarNumber(aadhaar: String): Boolean {
    return aadhaar.length == 12 && aadhaar.all { it.isDigit() }
}

@Composable
private fun OtpVerificationView(
    guarantor: GuarantorKycData,
    client: com.mifos.core.objects.client.Client,
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

        // Guarantor Info Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Guarantor Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${guarantor.fullName ?: "Unknown"}")
                Text("Mobile: ${guarantor.mobileNumber ?: "N/A"}")
                Text("Relationship: ${guarantor.relationshipToClient ?: "N/A"}")
                if (guarantor.isPrimaryGuarantor) {
                    Text(
                        text = "PRIMARY GUARANTOR",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                    text = "Aadhaar Number: ${guarantor.aadhaarNumber}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!otpGenerated) {
                    Text(
                        text = "An OTP will be sent to the mobile number registered with this Aadhaar.",
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
                        text = "OTP has been sent to the registered mobile number. Please enter it below:",
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

