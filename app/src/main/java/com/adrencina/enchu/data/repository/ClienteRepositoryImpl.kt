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
            val userProfileRef = firestore.collection("users").document(userId)
            
            val registration = userProfileRef.addSnapshotListener { profileSnap, profileError ->
                if (profileError != null) {
                    close(profileError)
                    return@addSnapshotListener
                }

                val organizationId = profileSnap?.getString("organizationId")
                
                val query = if (!organizationId.isNullOrEmpty()) {
                    firestore.collection("clientes").whereEqualTo("organizationId", organizationId)
                } else {
                    firestore.collection("clientes").whereEqualTo("userId", userId)
                }
                
                query.addSnapshotListener { snap, err ->
                    if (err != null) return@addSnapshotListener
                    if (snap != null) {
                        val clientes = snap.toObjects(Cliente::class.java)
                        trySend(clientes).isSuccess
                    }
                }
            }
            awaitClose { registration.remove() }
        }
    }

        override suspend fun saveCliente(cliente: Cliente): Result<Unit> {
            return try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    return Result.failure(Exception("Usuario no autenticado."))
                }
    
                // Obtener organizationId
                val userSnapshot = firestore.collection("users").document(userId).get().await()
                val organizationId = userSnapshot.getString("organizationId") ?: ""
    
                // Asignamos el ID del usuario al cliente y lo guardamos en la colección "clientes"
                firestore.collection("clientes").add(cliente.copy(userId = userId, organizationId = organizationId)).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
        override suspend fun updateCliente(cliente: Cliente): Result<Unit> {
            return try {
                firestore.collection("clientes").document(cliente.id).update(
                    mapOf(
                        "nombre" to cliente.nombre,
                        "dni" to cliente.dni,
                        "telefono" to cliente.telefono,
                        "email" to cliente.email,
                        "direccion" to cliente.direccion
                    )
                ).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        override suspend fun doesDniExist(dni: String): Boolean {
            return try {
                val userId = auth.currentUser?.uid ?: return false 
                
                val userSnapshot = firestore.collection("users").document(userId).get().await()
                val organizationId = userSnapshot.getString("organizationId")
    
                val query = if (organizationId != null) {
                     firestore.collection("clientes")
                        .whereEqualTo("organizationId", organizationId)
                        .whereEqualTo("dni", dni)
                        .limit(1)
                        .get()
                        .await()
                } else {
                    // Fallback
                     firestore.collection("clientes")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("dni", dni)
                        .limit(1)
                        .get()
                        .await()
                }
    
                !query.isEmpty
            } catch (e: Exception) {
                false
            }
        }}