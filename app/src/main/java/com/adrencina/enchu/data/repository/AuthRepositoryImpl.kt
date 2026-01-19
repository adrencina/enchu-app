package com.adrencina.enchu.data.repository

import com.adrencina.enchu.data.model.Organization
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
            
            // NO creamos el perfil automáticamente aquí.
            // La UI debe observar getUserProfile(). Si es null, redirigir a WelcomeScreen.
            
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
                    trySend(null) // Perfil no existe aún
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
                // 1. Crear nueva Organización para este usuario
                val newOrgRef = firestore.collection("organizations").document()
                // El ID del documento será el código de invitación (para simplificar MVP)
                // O podríamos generar un campo 'inviteCode' separado.
                val newOrg = Organization(
                    id = newOrgRef.id,
                    name = "Organización de ${user.displayName ?: "Usuario"}",
                    ownerId = user.uid,
                    members = listOf(user.uid)
                )
                newOrgRef.set(newOrg).await()

                // 2. Crear Perfil de Usuario vinculado a la Org
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
            // 1. Buscar la organización por ID (inviteCode)
            val orgRef = firestore.collection("organizations").document(inviteCode)
            val orgSnapshot = orgRef.get().await()

            if (!orgSnapshot.exists()) {
                return Result.failure(Exception("Código de organización inválido."))
            }

            val org = orgSnapshot.toObject(Organization::class.java) ?: return Result.failure(Exception("Error al leer organización."))
            
            // Lógica inteligente de roles:
            // Si soy el dueño original (reinstalé la app), recupero mi rol de OWNER.
            // Si ya soy miembro, mantengo mi estado (o asumo MEMBER/EMPLOYEE si no tengo perfil).
            val isOwner = org.ownerId == user.uid
            val isAlreadyMember = org.members.contains(user.uid)
            
            val targetRole = if (isOwner) "OWNER" else "EMPLOYEE"

            // 2. Actualizar la organización agregando al miembro (solo si NO estaba ya)
            if (!isAlreadyMember) {
                orgRef.update("members", com.google.firebase.firestore.FieldValue.arrayUnion(user.uid)).await()
            }

            // 3. Crear o actualizar el perfil del usuario vinculado
            // Esto restaura el acceso al usuario si había borrado la app
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
