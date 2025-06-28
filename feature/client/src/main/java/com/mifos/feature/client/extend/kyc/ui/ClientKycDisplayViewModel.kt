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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mifos.core.objects.client.extend.kyc.ClientKycData
import com.mifos.core.objects.client.extend.kyc.GuarantorKycData
import com.mifos.core.data.repository.extend.ClientKycRepository
import com.mifos.core.data.repository.extend.GuarantorKycRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling Client KYC display in client details screen
 * Placed in extend module to minimize changes to upstream files
 * Updated to use the new API layer consistently with GuarantorKycViewModel
 */
@HiltViewModel
class ClientKycDisplayViewModel @Inject constructor(
    private val clientKycRepository: ClientKycRepository,
    private val guarantorKycRepository: GuarantorKycRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClientKycDisplayUiState>(ClientKycDisplayUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadKycData(clientId: Int) {
        println("DEBUG: loadKycData called with clientId: $clientId")
        _uiState.value = ClientKycDisplayUiState.Loading
        
        viewModelScope.launch {
            try {
                // Load both client KYC and guarantor KYC in parallel using the new API layer
                var clientKyc: ClientKycData? = null
                var guarantorKycList: List<GuarantorKycData> = emptyList()
                
                // Client KYC - using new repository
                try {
                    val kycData = clientKycRepository.getClientKyc(clientId)
                    println("DEBUG: Client KYC API Response: $kycData")
                    
                    // Only set clientKyc if it contains actual data, not just default template
                    if (hasActualKycData(kycData)) {
                        clientKyc = kycData
                        println("DEBUG: Client KYC has actual data")
                    } else {
                        println("DEBUG: Client KYC is empty/default template")
                    }
                } catch (e: Exception) {
                    println("DEBUG: Client KYC API Error: ${e.message}")
                    // Client KYC might not exist, this is fine
                }
                
                // Guarantor KYC - using new repository  
                try {
                    guarantorKycList = guarantorKycRepository.getGuarantorKycList(clientId)
                    println("DEBUG: Guarantor KYC API Response: $guarantorKycList")
                } catch (e: Exception) {
                    println("DEBUG: Guarantor KYC API Error: ${e.message}")
                    // Guarantor KYC might not exist, this is fine
                }
                
                if (clientKyc != null || guarantorKycList.isNotEmpty()) {
                    _uiState.value = ClientKycDisplayUiState.Success(
                        clientKyc = clientKyc,
                        guarantorKycList = guarantorKycList
                    )
                } else {
                    _uiState.value = ClientKycDisplayUiState.Empty
                }
                
            } catch (e: Exception) {
                println("DEBUG: loadKycData error: ${e.message}")
                _uiState.value = ClientKycDisplayUiState.Error(e.message ?: "Failed to load KYC data")
            }
        }
    }
    
    /**
     * Helper function to check if ClientKycData contains actual data
     * Returns false if it's just an empty template with default values
     */
    private fun hasActualKycData(kycData: ClientKycData): Boolean {
        return when {
            // If any of the document numbers exist, it has actual data
            !kycData.panNumber.isNullOrBlank() -> true
            !kycData.aadhaarNumber.isNullOrBlank() -> true
            !kycData.voterIdNumber.isNullOrBlank() -> true
            !kycData.drivingLicenseNumber.isNullOrBlank() -> true
            !kycData.passportNumber.isNullOrBlank() -> true
            
            // If any verification status is true, it has been processed
            kycData.panVerified == true -> true
            kycData.aadhaarVerified == true -> true
            kycData.aadhaarOtpVerified == true -> true
            
            // If verification notes exist, it has been touched
            !kycData.manualVerificationNotes.isNullOrBlank() -> true
            
            // If it has an ID from backend (not just clientId), it exists in database
            kycData.id != null && kycData.id != 0L -> true
            
            // Otherwise, it's just a default template
            else -> false
        }
    }
}

/**
 * UI State for Client KYC Display
 */
sealed class ClientKycDisplayUiState {
    object Loading : ClientKycDisplayUiState()
    object Empty : ClientKycDisplayUiState()
    data class Success(
        val clientKyc: ClientKycData?,
        val guarantorKycList: List<GuarantorKycData>
    ) : ClientKycDisplayUiState()
    data class Error(val message: String) : ClientKycDisplayUiState()
} 