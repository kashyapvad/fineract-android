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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mifos.core.designsystem.component.MifosCircularProgress
import com.mifos.core.designsystem.component.MifosScaffold
import com.mifos.core.designsystem.component.MifosSweetError
import com.mifos.core.designsystem.icon.MifosIcons
import com.mifos.core.objects.client.ClientPayload
import com.mifos.feature.client.R

@Composable
internal fun OfflineClientOptionsScreen(
    clientPayloadId: Int,
    onBackPressed: () -> Unit,
    onNavigateToClientKyc: (Int) -> Unit,
    onNavigateToGuarantorKyc: (Int) -> Unit,
    viewModel: OfflineClientOptionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(clientPayloadId) {
        viewModel.loadClientPayload(clientPayloadId)
    }

    OfflineClientOptionsScreen(
        uiState = uiState,
        onBackPressed = onBackPressed,
        onNavigateToClientKyc = { onNavigateToClientKyc(clientPayloadId) },
        onNavigateToGuarantorKyc = { onNavigateToGuarantorKyc(clientPayloadId) },
        onRetry = { viewModel.loadClientPayload(clientPayloadId) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OfflineClientOptionsScreen(
    uiState: OfflineClientOptionsUiState,
    onBackPressed: () -> Unit,
    onNavigateToClientKyc: () -> Unit,
    onNavigateToGuarantorKyc: () -> Unit,
    onRetry: () -> Unit,
) {
    MifosScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feature_client_offline_client_options)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = MifosIcons.arrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is OfflineClientOptionsUiState.Loading -> {
                    MifosCircularProgress()
                }
                is OfflineClientOptionsUiState.Error -> {
                    MifosSweetError(message = uiState.message) {
                        onRetry()
                    }
                }
                is OfflineClientOptionsUiState.Success -> {
                    OfflineClientOptionsContent(
                        clientPayload = uiState.clientPayload,
                        onNavigateToClientKyc = onNavigateToClientKyc,
                        onNavigateToGuarantorKyc = onNavigateToGuarantorKyc,
                    )
                }
            }
        }
    }
}

@Composable
private fun OfflineClientOptionsContent(
    clientPayload: ClientPayload,
    onNavigateToClientKyc: () -> Unit,
    onNavigateToGuarantorKyc: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ClientInfoCard(clientPayload = clientPayload)
        }
        
        item {
            Text(
                text = "Available Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        val options = listOf(
            OfflineClientOption(
                title = "Client KYC",
                description = "Manage client Know Your Customer information",
                icon = Icons.Default.Person,
                onClick = onNavigateToClientKyc
            ),
            OfflineClientOption(
                title = "Guarantor KYC", 
                description = "Manage guarantor Know Your Customer information",
                icon = Icons.Default.AccountBox,
                onClick = onNavigateToGuarantorKyc
            )
        )
        
        items(options) { option ->
            OfflineClientOptionItem(option = option)
        }
    }
}

@Composable
private fun ClientInfoCard(clientPayload: ClientPayload) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Pending Sync",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Offline Client (Pending Sync)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val displayName = buildString {
                clientPayload.firstname?.let { append(it) }
                clientPayload.middlename?.let { 
                    if (isNotEmpty()) append(" ")
                    append(it)
                }
                clientPayload.lastname?.let { 
                    if (isNotEmpty()) append(" ")
                    append(it)
                }
            }.ifEmpty { "Unknown Client" }
            
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
            
            clientPayload.externalId?.let {
                Text(
                    text = "External ID: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            clientPayload.mobileNo?.let {
                Text(
                    text = "Mobile: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OfflineClientOptionItem(option: OfflineClientOption) {
    Card(
        onClick = option.onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class OfflineClientOption(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

@Preview(showBackground = true)
@Composable
private fun OfflineClientOptionsScreenPreview() {
    val sampleClientPayload = ClientPayload(
        id = 1,
        firstname = "John",
        lastname = "Doe",
        externalId = "EXT123",
        mobileNo = "1234567890"
    )
    
    OfflineClientOptionsScreen(
        uiState = OfflineClientOptionsUiState.Success(sampleClientPayload),
        onBackPressed = {},
        onNavigateToClientKyc = {},
        onNavigateToGuarantorKyc = {},
        onRetry = {}
    )
} 