/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.network.mappers.staffs

import com.mifos.core.objects.organisation.Staff
import com.mifos.core.objects.organisation.StaffResponse
import org.mifos.core.data.AbstractMapper

object StaffMapper : AbstractMapper<StaffResponse, Staff>() {
    override fun mapFromEntity(entity: StaffResponse): Staff {
        return Staff().apply {
            id = entity.id!!.toInt()
            firstname = entity.firstname
            lastname = entity.lastname
            displayName = entity.displayName
            officeId = entity.officeId!!.toInt()
            officeName = entity.officeName
            isLoanOfficer = entity.isLoanOfficer
            isActive = entity.isActive
        }
    }

    override fun mapToEntity(domainModel: Staff): StaffResponse {
        return StaffResponse(
            id = domainModel.id?.toLong(),
            firstname = domainModel.firstname,
            lastname = domainModel.lastname,
            displayName = domainModel.displayName,
            officeId = domainModel.officeId?.toLong(),
            officeName = domainModel.officeName,
            isLoanOfficer = domainModel.isLoanOfficer,
            isActive = domainModel.isActive,
        )
    }
}
