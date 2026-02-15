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
                
                // Usamos el Document directamente para la creación inicial
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
                    role = "OWNER",
                    status = "ACTIVE"
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
            val userRef = firestore.collection("users").document(user.uid)

            firestore.runTransaction { transaction ->
                val orgSnapshot = transaction.get(orgRef)
                if (!orgSnapshot.exists()) {
                    throw Exception("Código de organización inválido.")
                }

                val orgDoc = orgSnapshot.toObject(OrganizationDocument::class.java)
                    ?: throw Exception("Error al leer organización.")

                // Consultamos el perfil actual para preservar el lastRejectionTimestamp
                val currentProfileSnap = transaction.get(userRef)
                val lastRejection = if (currentProfileSnap.exists()) {
                    currentProfileSnap.getLong("lastRejectionTimestamp") ?: 0L
                } else 0L

                val isOwner = orgDoc.ownerId == user.uid
                val isAlreadyMember = orgDoc.members.contains(user.uid)
                val targetRole = if (isOwner) "OWNER" else "WORKER"
                val targetStatus = if (isOwner) "ACTIVE" else "PENDING"

                // 1. Unir el usuario a la lista de miembros de la organización
                if (!isAlreadyMember) {
                    val updatedMembers = orgDoc.members.toMutableList().apply { add(user.uid) }
                    transaction.update(orgRef, "members", updatedMembers)
                }

                // 2. Crear/Actualizar el perfil del usuario
                val newProfile = UserProfile(
                    id = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "",
                    organizationId = inviteCode,
                    role = targetRole,
                    status = targetStatus,
                    lastRejectionTimestamp = lastRejection
                )
                transaction.set(userRef, newProfile)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserRole(role: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            firestore.collection("users").document(userId).update("role", role).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}