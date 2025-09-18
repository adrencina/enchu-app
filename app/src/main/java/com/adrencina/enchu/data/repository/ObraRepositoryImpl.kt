package com.adrencina.enchu.data.repository

import com.adrencina.enchu.data.model.Obra
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ObraRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ObraRepository {

    override fun getObras(): Flow<List<Obra>> {
        val userId = auth.currentUser?.uid ?: return flowOf(emptyList())

        return callbackFlow {
            val listener = firestore.collection("obras")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val obras = snapshot.toObjects(Obra::class.java)
                        trySend(obras).isSuccess
                    }
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun saveObra(obra: Obra): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                // Devolvemos un fallo explícito si el usuario no está logueado
                return Result.failure(Exception("Usuario no autenticado."))
            }
            // Asignamos el ID del usuario actual a la obra antes de guardarla
            firestore.collection("obras").add(obra.copy(userId = userId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Capturamos cualquier otro error de Firestore
            Result.failure(e)
        }
    }
}