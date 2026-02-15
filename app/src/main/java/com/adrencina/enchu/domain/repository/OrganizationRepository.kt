package com.adrencina.enchu.domain.repository

import android.net.Uri
import com.adrencina.enchu.domain.model.Organization
import com.adrencina.enchu.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface OrganizationRepository {
    fun getOrganization(orgId: String): Flow<Organization?>
    fun getMembers(orgId: String): Flow<List<UserProfile>>
    suspend fun updateOrganization(organization: Organization): Result<Unit>
    suspend fun uploadLogo(orgId: String, uri: Uri): Result<String>
    suspend fun incrementStorageUsed(orgId: String, bytes: Long): Result<Unit>
    
    // Gestión de Miembros
    suspend fun removeMember(userId: String): Result<Unit>
    suspend fun updateMemberRole(userId: String, newRole: String): Result<Unit>
    suspend fun approveMember(userId: String): Result<Unit>
    fun getPendingMembersCount(orgId: String): Flow<Int>
}