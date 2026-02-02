package com.adrencina.enchu.data.repository

import com.adrencina.enchu.data.model.OrganizationDocument
import com.adrencina.enchu.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun firebaseSignInWithGoogle(token: String): Boolean {
        return try {
            val credential = GoogleAuthProvider.getCredential(token, null)
            auth.signInWithCredential(credential).await()
            auth.currentUser != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getUserProfile(): Flow<UserProfile?> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(UserProfile::class.java))
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getUserProfileById(userId: String): UserProfile? {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun createIndependentProfile(user: FirebaseUser): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(user.uid)
            val snapshot = userRef.get().await()

            if (!snapshot.exists()) {
                val newOrgRef = firestore.collection("organizations").document()
                
                // Usamos el Document directamente para la creación inicial (tiene defaults para campos opcionales)
                val newOrgDoc = OrganizationDocument(
                    id = newOrgRef.id,
                    name = "Organización de ${user.displayName ?: "Usuario"}",
                    ownerId = user.uid,
                    members = listOf(user.uid)
                )
                newOrgRef.set(newOrgDoc).await()

                val newProfile = UserProfile(
                    id = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "",
                    organizationId = newOrgRef.id,
                    role = "OWNER"
                )
                userRef.set(newProfile).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinOrganizationProfile(user: FirebaseUser, inviteCode: String): Result<Unit> {
        return try {
            val orgRef = firestore.collection("organizations").document(inviteCode)
            val orgSnapshot = orgRef.get().await()

            if (!orgSnapshot.exists()) {
                return Result.failure(Exception("Código de organización inválido."))
            }

            // Mapeamos a Document
            val orgDoc = orgSnapshot.toObject(OrganizationDocument::class.java) ?: return Result.failure(Exception("Error al leer organización."))
            
            val isOwner = orgDoc.ownerId == user.uid
            val isAlreadyMember = orgDoc.members.contains(user.uid)
            
            val targetRole = if (isOwner) "OWNER" else "EMPLOYEE"

            if (!isAlreadyMember) {
                orgRef.update("members", com.google.firebase.firestore.FieldValue.arrayUnion(user.uid)).await()
            }

            val userRef = firestore.collection("users").document(user.uid)
            val newProfile = UserProfile(
                id = user.uid,
                email = user.email ?: "",
                displayName = user.displayName ?: "",
                organizationId = inviteCode,
                role = targetRole
            )
            userRef.set(newProfile).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}