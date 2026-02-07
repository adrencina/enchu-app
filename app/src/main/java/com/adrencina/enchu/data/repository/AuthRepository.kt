package com.adrencina.enchu.data.repository

import com.adrencina.enchu.domain.model.Organization
import com.adrencina.enchu.data.model.UserProfile
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun firebaseSignInWithGoogle(token: String): Boolean
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun getUserProfileById(userId: String): UserProfile?
    suspend fun createIndependentProfile(user: FirebaseUser): Result<Unit>
    suspend fun joinOrganizationProfile(user: FirebaseUser, inviteCode: String): Result<Unit>
    suspend fun updateUserRole(role: String): Result<Unit>
    suspend fun signOut()
}
