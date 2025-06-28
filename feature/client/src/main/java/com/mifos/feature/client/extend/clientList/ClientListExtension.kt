/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.client.extend.clientList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mifos.core.objects.client.Client
import com.mifos.core.objects.client.ClientPayload

/**
 * Extension interface for client list functionality
 */
object ClientListExtension {
    
    /**
     * Determines if the offline mode should show pending clients
     */
    fun shouldShowPendingClients(
        syncedClients: List<Client>,
        pendingClients: List<ClientPayload>
    ): Boolean {
        return syncedClients.isNotEmpty() || pendingClients.isNotEmpty()
    }
    
    /**
     * Creates display items from both synced clients and pending payloads
     */
    fun createDisplayItems(
        syncedClients: List<Client>,
        pendingClients: List<ClientPayload>
    ): List<ClientDisplayItem> {
        val displayItems = mutableListOf<ClientDisplayItem>()
        
        // Add synced clients
        displayItems.addAll(syncedClients.map { ClientDisplayItem.fromClient(it) })
        
        // Add pending clients
        displayItems.addAll(pendingClients.map { ClientDisplayItem.fromClientPayload(it) })
        
        return displayItems.sortedBy { it.displayName }
    }
}

/**
 * Composable for displaying both synced and pending clients
 */
@Composable
fun LazyColumnForClientListDbWithPending(
    syncedClients: List<Client>,
    pendingClients: List<ClientPayload>,
    onClientClick: (Int) -> Unit,
    onOfflineClientClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayItems = ClientListExtension.createDisplayItems(syncedClients, pendingClients)
    
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(displayItems) { item ->
            ClientDisplayCard(
                item = item,
                onClick = {
                    if (item.isPending && item.clientPayload != null) {
                        item.clientPayload.id?.let { onOfflineClientClick(it) }
                    } else if (item.client != null) {
                        onClientClick(item.client.id ?: 0)
                    }
                }
            )
        }
    }
}

/**
 * Card component for displaying client information
 */
@Composable
private fun ClientDisplayCard(
    item: ClientDisplayItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Client name and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (item.isPending) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Pending Sync",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "Pending",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }
            
            // Account number
            item.accountNo?.let { accountNo ->
                Text(
                    text = "Account: $accountNo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Office information
            item.officeName?.let { officeName ->
                Text(
                    text = officeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Status information
            if (item.isPending && item.syncStatus != null) {
                Text(
                    text = "Status: ${item.syncStatus}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.syncStatus.contains("Error")) Color.Red else Color(0xFFFF9800)
                )
            }
        }
    }
}

/**
 * Unified client display model for both synced clients and pending payloads
 */
data class ClientDisplayItem(
    val id: Int?,
    val displayName: String,
    val accountNo: String?,
    val officeName: String?,
    val isActive: Boolean,
    val isPending: Boolean = false, // true for ClientPayload, false for synced Client
    val syncStatus: String? = null, // For pending clients
    val client: Client? = null, // For synced clients
    val clientPayload: ClientPayload? = null, // For pending clients
    val hasKycData: Boolean = false, // Indicates if KYC data exists
    val kycStatus: String? = null // KYC verification status
) {
    companion object {
        fun fromClient(client: Client): ClientDisplayItem {
            return ClientDisplayItem(
                id = client.id,
                displayName = client.displayName ?: "${client.firstname} ${client.lastname}",
                accountNo = client.accountNo,
                officeName = client.officeName,
                isActive = client.active ?: false,
                isPending = false,
                client = client,
                hasKycData = false, // TODO: Add KYC data check when KYC service is integrated
                kycStatus = "Not Available" // Default status for synced clients
            )
        }
        
        fun fromClientPayload(payload: ClientPayload): ClientDisplayItem {
            val displayName = buildString {
                payload.firstname?.let { append(it) }
                payload.middlename?.let { 
                    if (isNotEmpty()) append(" ")
                    append(it)
                }
                payload.lastname?.let { 
                    if (isNotEmpty()) append(" ")
                    append(it)
                }
            }.ifEmpty { "Unknown Client" }
            
            // Check if payload has KYC-related data (basic mobile number check for now)
            val hasKycData = payload.mobileNo?.isNotEmpty() == true
            val kycStatus = if (hasKycData) "Pending Sync" else "No KYC Data"
            
            return ClientDisplayItem(
                id = payload.id,
                displayName = displayName,
                accountNo = payload.externalId,
                officeName = "Office ID: ${payload.officeId}",
                isActive = payload.active ?: false,
                isPending = true,
                syncStatus = if (payload.errorMessage != null) "Sync Error" else "Pending Sync",
                clientPayload = payload,
                hasKycData = hasKycData,
                kycStatus = kycStatus
            )
        }
    }
} 