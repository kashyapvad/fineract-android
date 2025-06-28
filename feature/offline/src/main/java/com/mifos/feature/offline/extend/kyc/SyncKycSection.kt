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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mifos.core.objects.client.extend.ClientKycPayload
import com.mifos.core.objects.client.extend.GuarantorKycPayload
import com.mifos.feature.offline.R

/**
 * Composable component for KYC sync section.
 * 
 * This component can be included in the main sync screen to show KYC sync status
 * following the established patterns in the sync module.
 */
@Composable
fun SyncKycSection(
    modifier: Modifier = Modifier,
    viewModel: SyncKycViewModel = hiltViewModel(),
) {
    val uiState by viewModel.syncKycUiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadKycPayloads()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KYC Data Sync",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (val currentState = uiState) {
                is SyncKycUiState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is SyncKycUiState.Success -> {
                    KycSyncContent(
                        clientKycPayloads = currentState.clientKycPayloads,
                        guarantorKycPayloads = currentState.guarantorKycPayloads
                    )
                }

                is SyncKycUiState.Error -> {
                    Text(
                        text = "Error: ${currentState.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun KycSyncContent(
    clientKycPayloads: List<ClientKycPayload>,
    guarantorKycPayloads: List<GuarantorKycPayload>,
) {
    val totalPayloads = clientKycPayloads.size + guarantorKycPayloads.size

    if (totalPayloads == 0) {
        Text(
            text = "No KYC data to sync",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    // Summary
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Client KYC: ${clientKycPayloads.size}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Guarantor KYC: ${guarantorKycPayloads.size}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Text(
            text = "Total: $totalPayloads",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Info text - KYC syncs with client data
    Text(
        text = "KYC data will be synced automatically with client data",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Show first few payloads as preview
    if (clientKycPayloads.isNotEmpty()) {
        Text(
            text = "Client KYC Preview:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        clientKycPayloads.take(3).forEach { payload ->
            KycPayloadItem(
                title = "Client ID: ${payload.clientId}",
                status = payload.syncStatus,
                hasError = payload.errorMessage != null
            )
        }
        
        if (clientKycPayloads.size > 3) {
            Text(
                text = "... and ${clientKycPayloads.size - 3} more",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (guarantorKycPayloads.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Guarantor KYC Preview:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        guarantorKycPayloads.take(3).forEach { payload ->
            KycPayloadItem(
                title = "Client ID: ${payload.clientId} - ${payload.fullName}",
                status = payload.syncStatus,
                hasError = payload.errorMessage != null
            )
        }
        
        if (guarantorKycPayloads.size > 3) {
            Text(
                text = "... and ${guarantorKycPayloads.size - 3} more",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun KycPayloadItem(
    title: String,
    status: String,
    hasError: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )

        val statusColor = when {
            hasError -> MaterialTheme.colorScheme.error
            status == "SYNCED" -> Color(0xFF4CAF50)
            status == "PENDING_CREATE" -> Color(0xFFFF9800)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Text(
            text = when {
                hasError -> "ERROR"
                status == "SYNCED" -> "✓"
                else -> "⏳"
            },
            color = statusColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
} 