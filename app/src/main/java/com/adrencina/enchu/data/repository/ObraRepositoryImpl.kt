package com.adrencina.enchu.data.repository

import com.adrencina.enchu.data.model.Avance
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.model.PresupuestoItem
import com.adrencina.enchu.data.model.PresupuestoWithItems
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

    override suspend fun createObraFromPresupuesto(presupuesto: PresupuestoWithItems): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            
            // Obtener organizationId
            val userSnapshot = firestore.collection("users").document(userId).get().await()
            val organizationId = userSnapshot.getString("organizationId") ?: ""

            // 1. Crear Referencia para la nueva Obra
            val obraRef = firestore.collection("obras").document()
            val obraId = obraRef.id

            // 2. Mapear PresupuestoEntity -> Obra
            val entity = presupuesto.presupuesto
            val nuevaObra = Obra(
                id = obraId,
                userId = userId,
                organizationId = organizationId,
                clienteId = entity.clienteId,
                clienteNombre = "${entity.clienteNombre} ${entity.clienteApellido}".trim(),
                nombreObra = entity.titulo,
                direccion = entity.clienteDireccion,
                telefono = entity.clienteTelefono,
                estado = "En Proceso", // Estado inicial al aceptar
                budgetNumber = entity.numero,
                presupuestoTotal = entity.total,
                descuento = entity.descuento,
                validez = entity.validez,
                notas = entity.notas,
                fechaCreacion = null // Firestore pondrá el ServerTimestamp
            )

            // 3. PASO 1: Crear la Obra PRIMERO (sin batch para asegurar existencia)
            // Esto asegura que las reglas de seguridad que validan "parent.userId" funcionen para los items
            obraRef.set(nuevaObra).await()

            // 4. PASO 2: Crear Items en Batch (ahora que la obra existe)
            if (presupuesto.items.isNotEmpty()) {
                val batch = firestore.batch()
                presupuesto.items.forEach { itemEntity ->
                    val itemRef = obraRef.collection("presupuesto_items").document()
                    val itemFirestore = PresupuestoItem(
                        id = itemRef.id,
                        descripcion = itemEntity.descripcion,
                        cantidad = itemEntity.cantidad,
                        precioUnitario = itemEntity.precioUnitario,
                        tipo = itemEntity.tipo,
                        fechaCreacion = null // ServerTimestamp
                    )
                    batch.set(itemRef, itemFirestore)
                }
                batch.commit().await()
            }

            Result.success(obraId)
        } catch (e: Exception) {
            // Si falla la creación de items, idealmente deberíamos borrar la obra o alertar.
            // Por ahora, retornamos el fallo.
            Result.failure(e)
        }
    }

    override fun getObras(): Flow<List<Obra>> {

                        val userId = auth.currentUser?.uid ?: return flowOf(emptyList())

                

                        return callbackFlow {

                            val userProfileRef = firestore.collection("users").document(userId)

                            

                            // Usamos un listener para el perfil también, para reaccionar si le asignan una Org en tiempo real

                            val registration = userProfileRef.addSnapshotListener { profileSnap, profileError ->

                                if (profileError != null) {

                                    close(profileError)

                                    return@addSnapshotListener

                                }

                

                                val organizationId = profileSnap?.getString("organizationId")

                                

                                val obrasQuery = if (!organizationId.isNullOrEmpty()) {

                                    firestore.collection("obras").whereEqualTo("organizationId", organizationId)

                                } else {

                                    firestore.collection("obras").whereEqualTo("userId", userId)

                                }

                

                                // Aquí está el truco: necesitamos cancelar el listener anterior de obras si cambia la org

                                // pero dentro de este scope simple no es fácil.

                                // Para MVP: Registramos un nuevo listener.

                                

                                obrasQuery.addSnapshotListener { obrasSnap, obrasError ->

                                    if (obrasError != null) {

                                        // No cerramos el flow completo por un error transitorio aquí

                                        return@addSnapshotListener

                                    }

                                                                        if (obrasSnap != null) {

                                                                            val obras = obrasSnap.toObjects(Obra::class.java)

                                                                            trySend(obras.filter { it.id.isNotEmpty() && !it.isArchived }).isSuccess

                                                                        }

                                }

                            }

                            

                            awaitClose { registration.remove() }

                        }

                    }    override fun getArchivedObras(): Flow<List<Obra>> {
        val userId = auth.currentUser?.uid ?: return flowOf(emptyList())

        return callbackFlow {
            val userProfileRef = firestore.collection("users").document(userId)
            
            val registration = userProfileRef.addSnapshotListener { profileSnap, profileError ->
                if (profileError != null) {
                    close(profileError)
                    return@addSnapshotListener
                }

                val organizationId = profileSnap?.getString("organizationId")
                
                val obrasQuery = if (!organizationId.isNullOrEmpty()) {
                    firestore.collection("obras").whereEqualTo("organizationId", organizationId)
                } else {
                    firestore.collection("obras").whereEqualTo("userId", userId)
                }
                
                obrasQuery.addSnapshotListener { obrasSnap, obrasError ->
                    if (obrasError != null) return@addSnapshotListener
                    if (obrasSnap != null) {
                        val obras = obrasSnap.toObjects(Obra::class.java)
                        trySend(obras.filter { it.isArchived }).isSuccess
                    }
                }
            }
            awaitClose { registration.remove() }
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

        override suspend fun deleteObra(obraId: String): Result<Unit> {
            return try {
                firestore.collection("obras").document(obraId).delete().await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    override suspend fun saveObra(obra: Obra): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                return Result.failure(Exception("Usuario no autenticado."))
            }
            
            // Obtener organizationId del usuario actual
            val userSnapshot = firestore.collection("users").document(userId).get().await()
            val organizationId = userSnapshot.getString("organizationId") ?: "" // Fallback empty string if legacy
            
            // Guardamos con ambos IDs: userId (creador) y organizationId (dueño)
            firestore.collection("obras").add(obra.copy(userId = userId, organizationId = organizationId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
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
                    try {
                        val obra = snapshot.toObject(Obra::class.java)
                        if (obra != null) {
                            trySend(obra).isSuccess
                        } else {
                            close(Exception("Error: La obra existe pero no se pudo leer (datos corruptos)."))
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ObraRepo", "CRASH EVITADO: Error de tipo al leer Obra $obraId", e)
                        close(Exception("Error de formato de datos en la obra: ${e.message}"))
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
                    "direccion" to obra.direccion,
                    "clienteId" to obra.clienteId,
                    "clienteNombre" to obra.clienteNombre
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
                        // Mapeo Seguro: Si una tarea falla, no crashea toda la lista
                        val tareas = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(Tarea::class.java)
                            } catch (e: Exception) {
                                android.util.Log.e("ObraRepo", "Error mapeando Tarea ${doc.id}", e)
                                null
                            }
                        }
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
                        // Mapeo Seguro
                        val avances = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(Avance::class.java)
                            } catch (e: Exception) {
                                android.util.Log.e("ObraRepo", "Error mapeando Avance ${doc.id}", e)
                                null
                            }
                        }
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

    override fun getPresupuestoItems(obraId: String): Flow<List<PresupuestoItem>> {
        return callbackFlow {
            val listener = firestore.collection("obras").document(obraId).collection("presupuesto_items")
                .orderBy("fechaCreacion", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        // Mapeo Seguro
                        val items = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(PresupuestoItem::class.java)
                            } catch (e: Exception) {
                                android.util.Log.e("ObraRepo", "Error mapeando PresupuestoItem ${doc.id}", e)
                                null
                            }
                        }
                        trySend(items).isSuccess
                    }
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun addPresupuestoItem(obraId: String, item: PresupuestoItem): Result<Unit> {
        return try {
            firestore.collection("obras").document(obraId).collection("presupuesto_items")
                .add(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePresupuestoItem(obraId: String, item: PresupuestoItem): Result<Unit> {
        return try {
            firestore.collection("obras").document(obraId).collection("presupuesto_items").document(item.id)
                .set(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePresupuestoItem(obraId: String, itemId: String): Result<Unit> {
        return try {
            firestore.collection("obras").document(obraId).collection("presupuesto_items").document(itemId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}