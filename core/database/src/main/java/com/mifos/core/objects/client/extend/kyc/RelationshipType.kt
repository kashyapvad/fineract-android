/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.objects.client.extend.kyc

/**
 * Relationship types for guarantor KYC
 */
object RelationshipType {
    const val PARENT = "parent"
    const val SIBLING = "sibling" 
    const val SPOUSE = "spouse"
    const val BUSINESS_ASSOCIATE = "business_associate"
    const val OTHER = "other"
    
    /**
     * Get all available relationship types
     */
    fun getAllOptions(): List<String> = listOf(
        PARENT,
        SIBLING,
        SPOUSE,
        BUSINESS_ASSOCIATE,
        OTHER
    )
    
    /**
     * Get display name for relationship type
     */
    fun getDisplayName(relationshipType: String): String = when (relationshipType) {
        PARENT -> "Parent"
        SIBLING -> "Sibling"
        SPOUSE -> "Spouse"
        BUSINESS_ASSOCIATE -> "Business Associate"
        OTHER -> "Other"
        else -> relationshipType.replaceFirstChar { it.uppercaseChar() }
    }
} 