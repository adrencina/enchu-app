package com.adrencina.enchu.data.mapper

import com.adrencina.enchu.data.model.OrganizationDocument
import com.adrencina.enchu.domain.model.Organization

fun OrganizationDocument.toDomain(): Organization {
    return Organization(
        id = this.id,
        name = this.name,
        ownerId = this.ownerId,
        members = this.members,
        logoUrl = this.logoUrl,
        businessPhone = this.businessPhone,
        businessEmail = this.businessEmail,
        businessAddress = this.businessAddress,
        businessWeb = this.businessWeb,
        cuit = this.cuit,
        taxCondition = this.taxCondition,
        lastBudgetNumber = this.lastBudgetNumber,
        plan = this.plan,
        storageUsed = this.storageUsed,
        createdAt = this.createdAt
    )
}

fun Organization.toDocument(): OrganizationDocument {
    return OrganizationDocument(
        id = this.id,
        name = this.name,
        ownerId = this.ownerId,
        members = this.members,
        logoUrl = this.logoUrl,
        businessPhone = this.businessPhone,
        businessEmail = this.businessEmail,
        businessAddress = this.businessAddress,
        businessWeb = this.businessWeb,
        cuit = this.cuit,
        taxCondition = this.taxCondition,
        lastBudgetNumber = this.lastBudgetNumber,
        plan = this.plan,
        storageUsed = this.storageUsed,
        createdAt = this.createdAt
    )
}