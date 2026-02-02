package com.adrencina.enchu.domain.repository

import com.adrencina.enchu.domain.model.Organization
import kotlinx.coroutines.flow.Flow

interface OrganizationRepository {
    fun getOrganization(orgId: String): Flow<Organization?>
    suspend fun updateOrganization(organization: Organization): Result<Unit>
    suspend fun uploadLogo(orgId: String, uri: android.net.Uri): Result<String>
    suspend fun incrementStorageUsed(orgId: String, bytes: Long): Result<Unit>
}