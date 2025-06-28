/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.offline.extend.clientSync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mifos.core.designsystem.component.MifosTextFieldDropdown
import com.mifos.core.objects.client.ClientPayload
import com.mifos.core.objects.templates.clients.ClientsTemplate
import com.mifos.feature.offline.R

/**
 * Extension for client sync validation functionality.
 * This keeps validation logic separate from upstream sync files.
 */
object ClientSyncValidationExtension {
    /**
     * Checks if client payloads have validation issues
     */
    fun hasValidationIssues(payloads: List<ClientPayload>): Boolean {
        return payloads.any { payload ->
            (payload.officeId == null || payload.officeId == 0)
            // Note: Gender is optional, removed from validation
        }
    }

    /**
     * Filters payloads that need validation
     */
    fun getInvalidPayloads(payloads: List<ClientPayload>): List<ClientPayload> {
        return payloads.filter { payload ->
            (payload.officeId == null || payload.officeId == 0)
            // Note: Gender is optional, removed from validation
        }
    }
}

@Composable
fun ClientValidationScreen(
    invalidPayloads: List<ClientPayload>,
    clientTemplate: ClientsTemplate?,
    isUpdateAndSyncInProgress: Boolean = false,
    onBackPressed: () -> Unit,
    onValidationComplete: (List<Triple<ClientPayload, Int?, Int?>>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedOfficeIds by remember { mutableStateOf(invalidPayloads.map { it.officeId ?: 0 }) }
    var selectedGenderIds by remember { mutableStateOf(invalidPayloads.map { it.genderId ?: 0 }) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Text(
            text = "Fix Client Data",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "The following clients have missing required data. Please complete the information before syncing:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(invalidPayloads) { index, payload ->
                ClientValidationCard(
                    payload = payload,
                    clientTemplate = clientTemplate,
                    selectedOfficeId = selectedOfficeIds.getOrNull(index) ?: 0,
                    selectedGenderId = selectedGenderIds.getOrNull(index) ?: 0,
                    onOfficeSelected = { officeId ->
                        selectedOfficeIds =
                            selectedOfficeIds.toMutableList().apply {
                                if (index < size) this[index] = officeId
                            }
                    },
                    onGenderSelected = { genderId ->
                        selectedGenderIds =
                            selectedGenderIds.toMutableList().apply {
                                if (index < size) this[index] = genderId
                            }
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = onBackPressed,
                modifier = Modifier.weight(1f),
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    // Collect all updates
                    val updates =
                        invalidPayloads.mapIndexedNotNull { index, payload ->
                            val officeId = selectedOfficeIds.getOrNull(index)?.takeIf { it > 0 }
                            val genderId = selectedGenderIds.getOrNull(index)?.takeIf { it > 0 }
                            if (officeId != null) { // Only require office, gender is optional
                                Triple(payload, officeId, genderId)
                            } else {
                                null
                            }
                        }
                    onValidationComplete(updates)
                },
                modifier = Modifier.weight(1f),
                enabled = selectedOfficeIds.all { it > 0 } && !isUpdateAndSyncInProgress, // Only require office selection
            ) {
                if (isUpdateAndSyncInProgress) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Syncing...")
                    }
                } else {
                    Text("Update & Sync")
                }
            }
        }
    }
}

@Composable
private fun ClientValidationCard(
    payload: ClientPayload,
    clientTemplate: ClientsTemplate?,
    selectedOfficeId: Int,
    selectedGenderId: Int,
    onOfficeSelected: (Int) -> Unit,
    onGenderSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedOfficeName by remember { mutableStateOf("") }
    var selectedGenderName by remember { mutableStateOf("") }

    // Initialize selected values if available
    LaunchedEffect(selectedOfficeId) {
        if (selectedOfficeId > 0) {
            clientTemplate?.officeOptions?.find { it.id == selectedOfficeId }?.let { office ->
                selectedOfficeName = office.name
            }
        }
    }

    LaunchedEffect(selectedGenderId) {
        if (selectedGenderId > 0) {
            clientTemplate?.genderOptions?.find { it.id == selectedGenderId }?.let { gender ->
                selectedGenderName = gender.name
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "${payload.firstname} ${payload.lastname}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Office Selection
            if (clientTemplate?.officeOptions?.isNotEmpty() == true) {
                MifosTextFieldDropdown(
                    value = selectedOfficeName,
                    onValueChanged = { selectedOfficeName = it },
                    onOptionSelected = { index, value ->
                        selectedOfficeName = value
                        val officeId = clientTemplate.officeOptions[index].id
                        onOfficeSelected(officeId)
                    },
                    label = R.string.feature_offline_office_required,
                    options = clientTemplate.officeOptions.map { it.name },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(
                    text = "Office options not available (offline mode)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gender Selection (Optional)
            if (clientTemplate?.genderOptions?.isNotEmpty() == true) {
                MifosTextFieldDropdown(
                    value = selectedGenderName,
                    onValueChanged = { selectedGenderName = it },
                    onOptionSelected = { index, value ->
                        selectedGenderName = value
                        val genderId = clientTemplate.genderOptions[index].id
                        onGenderSelected(genderId)
                    },
                    label = R.string.feature_offline_gender_optional,
                    options = clientTemplate.genderOptions.map { it.name },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(
                    text = "Gender options not available (offline mode)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
