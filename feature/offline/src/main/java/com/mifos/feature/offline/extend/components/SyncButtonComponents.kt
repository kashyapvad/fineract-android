/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.offline.extend.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mifos.core.designsystem.icon.MifosIcons
import com.mifos.feature.offline.extend.clientSync.SyncStatus

/**
 * Reusable sync button content that adapts to different sync states.
 * Eliminates duplication between main sync and individual sync buttons.
 */
@Composable
fun SyncButtonContent(
    syncState: SyncButtonState,
    modifier: Modifier = Modifier
) {
    when (syncState) {
        is SyncButtonState.Idle -> {
            Icon(
                MifosIcons.sync, 
                contentDescription = null, 
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(syncState.label)
        }
        is SyncButtonState.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(syncState.label)
        }
        is SyncButtonState.Success -> {
            Text(syncState.label)
        }
        is SyncButtonState.Failed -> {
            Text(syncState.label)
        }
    }
}

/**
 * Main sync button for bulk operations
 */
@Composable
fun MainSyncButton(
    isBulkSyncing: Boolean,
    isAnyIndividualSyncing: Boolean,
    isExtensionSyncing: Boolean,
    payloadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = when {
        isBulkSyncing -> SyncButtonState.Loading("Syncing All...")
        isExtensionSyncing -> SyncButtonState.Loading("Update & Sync...")
        isAnyIndividualSyncing -> SyncButtonState.Loading("Individual Sync...")
        else -> SyncButtonState.Idle("Sync All ($payloadCount)")
    }
    
    // Block bulk sync if any other sync operation is happening
    val isBlocked = isBulkSyncing || isAnyIndividualSyncing || isExtensionSyncing
    
    Button(
        onClick = onClick,
        enabled = !isBlocked,
        modifier = modifier
    ) {
        SyncButtonContent(state)
    }
}

/**
 * Individual sync button for single client operations
 */
@Composable
fun IndividualSyncButton(
    syncStatus: SyncStatus?,
    isBulkSyncing: Boolean = false,
    isExtensionSyncing: Boolean = false,
    isAnyIndividualSyncing: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isThisClientSyncing = syncStatus == SyncStatus.SYNCING || syncStatus == SyncStatus.KYC_SYNCING
    
    val state = when {
        isBulkSyncing -> SyncButtonState.Loading("Bulk Sync...")
        isExtensionSyncing -> SyncButtonState.Loading("Extension Sync...")
        isAnyIndividualSyncing && !isThisClientSyncing -> SyncButtonState.Loading("Other Sync...")
        syncStatus == SyncStatus.SYNCING -> SyncButtonState.Loading("Syncing...")
        syncStatus == SyncStatus.KYC_SYNCING -> SyncButtonState.Loading("Syncing...")
        syncStatus == SyncStatus.SUCCESS -> SyncButtonState.Success("Synced ✓")
        syncStatus == SyncStatus.FAILED -> SyncButtonState.Failed("Retry")
        else -> SyncButtonState.Idle("Sync")
    }
    
    // Block if any sync operation is happening globally
    val isBlocked = isBulkSyncing || isExtensionSyncing || isAnyIndividualSyncing
    
    Button(
        onClick = onClick,
        enabled = !isBlocked,
        modifier = modifier
    ) {
        SyncButtonContent(state)
    }
}

/**
 * Sealed class representing different button states
 */
sealed class SyncButtonState {
    data class Idle(val label: String) : SyncButtonState()
    data class Loading(val label: String) : SyncButtonState()
    data class Success(val label: String) : SyncButtonState()
    data class Failed(val label: String) : SyncButtonState()
} 