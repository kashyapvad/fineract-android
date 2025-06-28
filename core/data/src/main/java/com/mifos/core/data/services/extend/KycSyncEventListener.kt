/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.data.services.extend

import android.util.Log
import com.mifos.core.common.extend.events.ClientCreatedEvent
import com.mifos.core.common.extend.events.ClientEventListener
import com.mifos.core.common.extend.events.ClientEventBus
import com.mifos.core.common.extend.events.ClientSyncCompletedEvent
import com.mifos.core.common.utils.FileUtils.LOG_TAG
import rx.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Event listener that automatically handles KYC sync when clients are created.
 * This approach eliminates the need to modify upstream ViewModels.
 */
@Singleton
class KycSyncEventListener @Inject constructor(
    private val clientKycSyncService: ClientKycSyncService
) : ClientEventListener {

    override fun onClientCreated(event: ClientCreatedEvent) {
        val localClientId = event.localClientId
        if (localClientId != null) {
            // Offline creation case - sync local KYC payloads to server
            clientKycSyncService.syncKycForClient(event.serverClientId, localClientId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { success ->
                        Log.d(LOG_TAG, "KYC sync completed for client ${event.serverClientId}: $success")
                        // Post completion event
                        ClientEventBus.post(
                            ClientSyncCompletedEvent(
                                serverClientId = event.serverClientId,
                                localClientId = localClientId,
                                success = success,
                                source = event.source
                            )
                        )
                    },
                    { error ->
                        Log.e(LOG_TAG, "KYC sync failed for client ${event.serverClientId}", error)
                        // Post completion event with error
                        ClientEventBus.post(
                            ClientSyncCompletedEvent(
                                serverClientId = event.serverClientId,
                                localClientId = localClientId,
                                success = false,
                                error = error.message,
                                source = event.source
                            )
                        )
                    }
                )
        } else {
            // Direct creation case - no local KYC payloads to sync, immediate completion
            ClientEventBus.post(
                ClientSyncCompletedEvent(
                    serverClientId = event.serverClientId,
                    localClientId = null,
                    success = true,
                    source = event.source
                )
            )
        }
    }
} 