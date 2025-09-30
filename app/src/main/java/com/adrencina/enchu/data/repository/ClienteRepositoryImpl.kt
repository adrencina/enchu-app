package com.adrencina.enchu.data.repository

import com.adrencina.enchu.data.model.Cliente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ClienteRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ClienteRepository {

    override fun getClientes(): Flow<List<Cliente>> {
        val userId = auth.currentUser?.uid ?: return flowOf(emptyList())

        return callbackFlow {
            val listener = firestore.collection("clientes")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val clientes = snapshot.toObjects(Cliente::class.java)
                        trySend(clientes).isSuccess
                    }
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun saveCliente(cliente: Cliente): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                return Result.failure(Exception("Usuario no autenticado."))
            }
            // Asignamos el ID del usuario al cliente y lo guardamos en la colección "clientes"
            firestore.collection("clientes").add(cliente.copy(userId = userId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}