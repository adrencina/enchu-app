package com.adrencina.enchu.data.repository

import com.adrencina.enchu.data.model.Organization
import kotlinx.coroutines.flow.Flow

interface OrganizationRepository {
    fun getOrganization(orgId: String): Flow<Organization?>
    suspend fun updateOrganization(organization: Organization): Result<Unit>
    suspend fun uploadLogo(orgId: String, uri: android.net.Uri): Result<String> // Returns URL
}
