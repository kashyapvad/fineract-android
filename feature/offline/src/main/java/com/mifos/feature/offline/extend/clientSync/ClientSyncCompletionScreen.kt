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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mifos.core.designsystem.icon.MifosIcons

@Composable
fun ClientSyncCompletionScreen(
    syncResults: List<ClientSyncResult>,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val successCount = syncResults.count { it.isSuccess }
    val failureCount = syncResults.count { !it.isSuccess }
    val isAllSuccess = failureCount == 0

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header Icon and Title
        Icon(
            imageVector = if (isAllSuccess) MifosIcons.check else MifosIcons.error,
            contentDescription = null,
            tint = if (isAllSuccess) Color(0xFF4CAF50) else Color(0xFFFF9800),
            modifier = Modifier.size(64.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isAllSuccess) "Sync Completed Successfully!" else "Sync Completed with Issues",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Summary
        Text(
            text =
                buildString {
                    append("$successCount client(s) synced successfully")
                    if (failureCount > 0) {
                        append("\n$failureCount client(s) failed to sync")
                    }
                },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Results List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(syncResults) { result ->
                ClientSyncResultCard(result = result)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Finish Button
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Finish")
        }
    }
}

@Composable
private fun ClientSyncResultCard(
    result: ClientSyncResult,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (result.isSuccess) {
                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                    } else {
                        Color(0xFFF44336).copy(alpha = 0.1f)
                    },
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (result.isSuccess) MifosIcons.check else MifosIcons.error,
                contentDescription = null,
                tint = if (result.isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.clientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )

                Text(
                    text = if (result.isSuccess) "Synced successfully" else (result.errorMessage ?: "Sync failed"),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (result.isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336),
                )
            }
        }
    }
}
