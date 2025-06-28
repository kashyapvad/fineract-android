/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.client.extend.syncValidation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mifos.feature.client.extend.preSyncValidation.PreSyncValidationViewModel

/**
 * Enhanced sync validation component that checks for validation issues before syncing
 */
@Composable
fun EnhancedSyncDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onProceedWithValidation: () -> Unit,
    onProceedWithoutValidation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val validationViewModel: PreSyncValidationViewModel = hiltViewModel()
    val uiState by validationViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by validationViewModel.isLoading.collectAsStateWithLifecycle()
    
    var hasValidationIssues by remember { mutableStateOf(false) }
    var invalidPayloadsCount by remember { mutableStateOf(0) }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            validationViewModel.loadInvalidClientPayloads()
        }
    }

    LaunchedEffect(uiState) {
        when (val currentState = uiState) {
            is com.mifos.feature.client.extend.preSyncValidation.PreSyncValidationUiState.ShowInvalidPayloads -> {
                hasValidationIssues = currentState.invalidPayloads.isNotEmpty()
                invalidPayloadsCount = currentState.invalidPayloads.size
            }
            else -> {
                hasValidationIssues = false
                invalidPayloadsCount = 0
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Sync Client Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column {
                    when {
                        isLoading -> {
                            Text("Checking for validation issues...")
                        }
                        
                        hasValidationIssues -> {
                            Text(
                                text = "Warning: Found $invalidPayloadsCount client(s) with missing required data.",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Missing office selection\n• Missing gender information",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("These clients may fail to sync. Would you like to fix these issues first?")
                        }
                        
                        else -> {
                            Text("All client data appears valid. Ready to sync.")
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (hasValidationIssues) {
                        Button(
                            onClick = onProceedWithoutValidation,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Text("Sync Anyway")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = onProceedWithValidation,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text("Fix Issues First")
                        }
                    } else {
                        Button(
                            onClick = onProceedWithoutValidation,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text("Proceed to Sync")
                        }
                    }
                }
            },
            dismissButton = {},
        )
    }
}

/**
 * Extension interface to provide enhanced sync validation functionality
 */
interface SyncValidationExtension {
    @Composable
    fun EnhancedSyncButton(
        onSyncWithValidation: () -> Unit,
        onSyncWithoutValidation: () -> Unit,
        modifier: Modifier,
    )
}

/**
 * Default implementation of sync validation extension
 */
class DefaultSyncValidationExtension : SyncValidationExtension {
    @Composable
    override fun EnhancedSyncButton(
        onSyncWithValidation: () -> Unit,
        onSyncWithoutValidation: () -> Unit,
        modifier: Modifier,
    ) {
        var showDialog by remember { mutableStateOf(false) }

        Button(
            onClick = { showDialog = true },
            modifier = modifier,
        ) {
            Text("Sync Clients")
        }

        EnhancedSyncDialog(
            showDialog = showDialog,
            onDismiss = { showDialog = false },
            onProceedWithValidation = {
                showDialog = false
                onSyncWithValidation()
            },
            onProceedWithoutValidation = {
                showDialog = false
                onSyncWithoutValidation()
            },
        )
    }
} 