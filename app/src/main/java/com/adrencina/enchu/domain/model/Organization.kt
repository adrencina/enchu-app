package com.adrencina.enchu.domain.model

import java.util.Date

data class Organization(
    val id: String,
    val name: String,
    val ownerId: String,
    val members: List<String>,
    val logoUrl: String,
    val businessPhone: String,
    val businessEmail: String,
    val businessAddress: String,
    val businessWeb: String,
    val cuit: String,
    val taxCondition: String,
    val lastBudgetNumber: Int,
    val plan: String,
    val storageUsed: Long,
    val createdAt: Date?
)
