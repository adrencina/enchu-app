package com.adrencina.enchu.data.repository

import com.adrencina.enchu.data.model.Avance
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.model.Tarea
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
                            trySend(obras.filter { !it.isArchived }).isSuccess
                        }
                    }
                awaitClose { listener.remove() }
            }
        }
    
        override fun getArchivedObras(): Flow<List<Obra>> {
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
                            trySend(obras.filter { it.isArchived }).isSuccess
                        }
                    }
                awaitClose { listener.remove() }
            }
        }
    
        override suspend fun archiveObra(obraId: String): Result<Unit> {
            return try {
                firestore.collection("obras").document(obraId).update("isArchived", true).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
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

    override fun getObraById(obraId: String): Flow<Obra> {
        return callbackFlow {
            val docRef = firestore.collection("obras").document(obraId)

            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val obra = snapshot.toObject(Obra::class.java)
                    if (obra != null) {
                        trySend(obra).isSuccess
                    } else {
                        close(Exception("Error al parsear los datos de la obra."))
                    }
                } else {
                    close(Exception("La obra con el ID especificado no existe."))
                }
            }

            awaitClose { listener.remove() }
        }
    }

    override suspend fun updateObra(obra: Obra): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                return Result.failure(Exception("Usuario no autenticado."))
            }
            // Actualizamos solo los campos que pueden ser modificados
            firestore.collection("obras").document(obra.id).update(
                mapOf(
                    "nombreObra" to obra.nombreObra,
                    "descripcion" to obra.descripcion,
                    "estado" to obra.estado,
                    "telefono" to obra.telefono,
                    "direccion" to obra.direccion
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTareas(obraId: String): Flow<List<Tarea>> {
        return callbackFlow {
            val listener = firestore.collection("obras").document(obraId).collection("tareas")
                .orderBy("fechaCreacion") // Ordenamos por fecha
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val tareas = snapshot.toObjects(Tarea::class.java)
                        trySend(tareas).isSuccess
                    }
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun addTarea(obraId: String, tarea: Tarea): Result<Unit> {
        return try {
            firestore.collection("obras").document(obraId).collection("tareas")
                .add(tarea).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTareaStatus(obraId: String, tareaId: String, completada: Boolean): Result<Unit> {
        return try {
            firestore.collection("obras").document(obraId).collection("tareas").document(tareaId)
                .update("completada", completada).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTarea(obraId: String, tareaId: String): Result<Unit> {
        return try {
            firestore.collection("obras").document(obraId).collection("tareas").document(tareaId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAvances(obraId: String): Flow<List<Avance>> {
        return callbackFlow {
            val listener = firestore.collection("obras").document(obraId).collection("avances")
                .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val avances = snapshot.toObjects(Avance::class.java)
                        trySend(avances).isSuccess
                    }
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun addAvance(obraId: String, avance: Avance): Result<Unit> {
        return try {
            firestore.collection("obras").document(obraId).collection("avances")
                .add(avance).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAvance(obraId: String, avanceId: String): Result<Unit> {
        return try {
            firestore.collection("obras").document(obraId).collection("avances").document(avanceId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}