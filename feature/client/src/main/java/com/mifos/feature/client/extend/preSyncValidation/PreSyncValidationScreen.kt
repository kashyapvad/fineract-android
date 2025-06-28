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

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mifos.core.designsystem.component.MifosCircularProgress
import com.mifos.core.designsystem.component.MifosScaffold
import com.mifos.core.designsystem.component.MifosTextFieldDropdown
import com.mifos.core.designsystem.icon.MifosIcons
import com.mifos.core.objects.client.ClientPayload
import com.mifos.core.objects.templates.clients.ClientsTemplate
import com.mifos.feature.client.R

@Composable
internal fun PreSyncValidationScreenRoute(
    viewModel: PreSyncValidationViewModel = hiltViewModel(),
    clientId: Int? = null,
    onBackPressed: () -> Unit,
    onValidationComplete: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = clientId) {
        println("🔄 PreSyncValidationScreenRoute: Loading data for clientId: $clientId")
        viewModel.loadInvalidClientPayloads(clientId)
        viewModel.loadClientTemplate()
    }

    PreSyncValidationScreen(
        uiState = uiState,
        isLoading = isLoading,
        onBackPressed = onBackPressed,
        onUpdatePayload = { payload -> viewModel.updateClientPayload(payload) },
        onValidationComplete = onValidationComplete,
    )
}

@Composable
internal fun PreSyncValidationScreen(
    uiState: PreSyncValidationUiState,
    isLoading: Boolean,
    onBackPressed: () -> Unit,
    onUpdatePayload: (ClientPayload) -> Unit,
    onValidationComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    MifosScaffold(
        modifier = modifier,
        isAppBarPresent = true,
        icon = MifosIcons.arrowBack,
        title = "Validate Client Data",
        onBackPressed = onBackPressed,
        snackbarHostState = null,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            when {
                isLoading -> {
                    MifosCircularProgress()
                }
                
                uiState is PreSyncValidationUiState.ShowInvalidPayloads && uiState.invalidPayloads.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "No validation needed",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "All client payloads have valid data",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onValidationComplete,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Proceed to Sync")
                        }
                    }
                }

                uiState is PreSyncValidationUiState.ShowInvalidPayloads -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    ) {
                        Text(
                            text = "Validation Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please complete the required fields for the following clients before syncing:",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                        ) {
                            items(uiState.invalidPayloads) { payload ->
                                ClientPayloadValidationCard(
                                    payload = payload,
                                    clientTemplate = uiState.clientTemplate,
                                    onUpdatePayload = onUpdatePayload,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (uiState.invalidPayloads.all { it.officeId != null && it.officeId!! > 0 }) {
                                    onValidationComplete()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Please complete all required fields",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text("Proceed to Sync")
                        }
                    }
                }

                uiState is PreSyncValidationUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "Error loading data",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onBackPressed,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Back")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientPayloadValidationCard(
    payload: ClientPayload,
    clientTemplate: ClientsTemplate?,
    onUpdatePayload: (ClientPayload) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedOffice by remember { mutableStateOf("") }
    var selectedOfficeId by remember { mutableStateOf(payload.officeId) }
    var selectedGender by remember { mutableStateOf("") }
    var selectedGenderId by remember { mutableStateOf(payload.genderId) }

    // Initialize selected values if they exist
    LaunchedEffect(clientTemplate, payload) {
        if (clientTemplate != null) {
            // Set initial office selection
            payload.officeId?.let { officeId ->
                clientTemplate.officeOptions?.find { it.id == officeId }?.let { office ->
                    selectedOffice = office.name ?: ""
                }
            }
            
            // Set initial gender selection
            payload.genderId?.let { genderId ->
                clientTemplate.genderOptions?.find { it.id == genderId }?.let { gender ->
                    selectedGender = gender.name ?: ""
                }
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (payload.officeId == null || payload.officeId == 0) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "${payload.firstname ?: ""} ${payload.lastname ?: ""}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            
            if (!payload.mobileNo.isNullOrEmpty()) {
                Text(
                    text = "Mobile: ${payload.mobileNo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Office selection
            if (payload.officeId == null || payload.officeId == 0) {
                Text(
                    text = "Office is required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                
                clientTemplate?.officeOptions?.let { offices ->
                    if (offices.isNotEmpty()) {
                        MifosTextFieldDropdown(
                            value = selectedOffice,
                            onValueChanged = { selectedOffice = it },
                            onOptionSelected = { index, value ->
                                selectedOffice = value
                                selectedOfficeId = offices[index].id
                                
                                // Update the payload
                                val updatedPayload = payload.copy(officeId = selectedOfficeId)
                                onUpdatePayload(updatedPayload)
                            },
                            label = R.string.feature_client_office_name_mandatory,
                            options = offices.map { it.name ?: "" },
                            readOnly = true,
                        )
                    }
                }
            } else {
                Text(
                    text = "✓ Office selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gender information (display only, not required for validation)
            if (payload.genderId != null && payload.genderId!! > 0) {
                Text(
                    text = "✓ Gender selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    text = "Gender not specified (optional)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
} 