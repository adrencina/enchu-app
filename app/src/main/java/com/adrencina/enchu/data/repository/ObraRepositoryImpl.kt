package com.adrencina.enchu.data.repository

import com.adrencina.enchu.data.mapper.toDocument
import com.adrencina.enchu.data.mapper.toDomain
import com.adrencina.enchu.data.model.AvanceDocument
import com.adrencina.enchu.data.model.MovimientoDocument
import com.adrencina.enchu.data.model.ObraDocument
import com.adrencina.enchu.data.model.PresupuestoItemDocument
import com.adrencina.enchu.data.model.TareaDocument
import com.adrencina.enchu.domain.common.Resource
import com.adrencina.enchu.domain.model.Avance
import com.adrencina.enchu.domain.model.EstadoObra
import com.adrencina.enchu.domain.model.Movimiento
import com.adrencina.enchu.domain.model.Obra
import com.adrencina.enchu.domain.model.Presupuesto
import com.adrencina.enchu.domain.model.PresupuestoItem
import com.adrencina.enchu.domain.model.Tarea
import com.adrencina.enchu.domain.repository.ObraRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class ObraRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ObraRepository {

    override suspend fun createObraFromPresupuesto(presupuesto: Presupuesto): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            val userSnapshot = firestore.collection("users").document(userId).get().await()
            val organizationId = userSnapshot.getString("organizationId") ?: ""

            val obraRef = firestore.collection("obras").document()
            val obraId = obraRef.id

            val nuevaObraDomain = Obra(
                id = obraId,
                userId = userId,
                organizationId = organizationId,
                clienteId = presupuesto.clienteId,
                clienteNombre = "${presupuesto.clienteNombre} ${presupuesto.clienteApellido}".trim(),
                nombreObra = presupuesto.titulo,
                descripcion = presupuesto.notas,
                direccion = presupuesto.clienteDireccion,
                telefono = presupuesto.clienteTelefono,
                estado = EstadoObra.EN_PROGRESO,
                budgetNumber = presupuesto.numero,
                presupuestoTotal = presupuesto.total,
                descuento = presupuesto.descuento,
                validez = presupuesto.validez,
                notas = presupuesto.notas,
                fechaCreacion = Date(),
                lastActivity = Date()
            )
            
            // Escritura Optimista
            firestore.collection("obras").document(obraId).set(nuevaObraDomain.toDocument())

            if (presupuesto.items.isNotEmpty()) {
                val batch = firestore.batch()
                presupuesto.items.forEach { itemDomain ->
                    val itemRef = obraRef.collection("presupuesto_items").document()
                    val itemDoc = PresupuestoItemDocument(
                        id = itemRef.id,
                        userId = userId,
                        organizationId = organizationId,
                        descripcion = itemDomain.descripcion,
                        cantidad = itemDomain.cantidad,
                        precioUnitario = itemDomain.precioUnitario,
                        tipo = itemDomain.tipo
                    )
                    batch.set(itemRef, itemDoc)
                }
                batch.commit()
            }

            Result.success(obraId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getObras(): Flow<Resource<List<Obra>>> {
        val userId = auth.currentUser?.uid ?: return flowOf(Resource.Error("Usuario no autenticado"))
        return callbackFlow {
            trySend(Resource.Loading())
            val userProfileRef = firestore.collection("users").document(userId)
            var obrasRegistration: com.google.firebase.firestore.ListenerRegistration? = null
            
            val registration = userProfileRef.addSnapshotListener { profileSnap, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error al obtener perfil"))
                    return@addSnapshotListener
                }

                val organizationId = profileSnap?.getString("organizationId")
                val obrasQuery = if (!organizationId.isNullOrEmpty()) {
                    firestore.collection("obras")
                        .whereEqualTo("organizationId", organizationId)
                        .orderBy("lastActivity", com.google.firebase.firestore.Query.Direction.DESCENDING)
                } else {
                    firestore.collection("obras")
                        .whereEqualTo("userId", userId)
                        .orderBy("lastActivity", com.google.firebase.firestore.Query.Direction.DESCENDING)
                }
                
                obrasRegistration?.remove()
                
                obrasRegistration = obrasQuery.addSnapshotListener { obrasSnap, obrasError ->
                    if (obrasError != null) {
                        trySend(Resource.Error(obrasError.message ?: "Error al cargar obras"))
                        return@addSnapshotListener
                    }
                    
                    if (obrasSnap != null) {
                        try {
                            val obrasDocs = obrasSnap.toObjects(ObraDocument::class.java)
                            val obrasList = obrasDocs.filter { it.id.isNotEmpty() && !it.isArchived }.map { it.toDomain() }
                            trySend(Resource.Success(obrasList))
                        } catch (e: Exception) {
                            trySend(Resource.Error("Error de datos: ${e.message}"))
                        }
                    }
                }
            }
            awaitClose { 
                registration.remove()
                obrasRegistration?.remove()
            }
        }
    }

    override fun getArchivedObras(): Flow<Resource<List<Obra>>> {
        val userId = auth.currentUser?.uid ?: return flowOf(Resource.Error("Usuario no autenticado"))
        return callbackFlow {
            trySend(Resource.Loading())
            val userProfileRef = firestore.collection("users").document(userId)
            var obrasRegistration: com.google.firebase.firestore.ListenerRegistration? = null
            
            val registration = userProfileRef.addSnapshotListener { profileSnap, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error al obtener perfil"))
                    return@addSnapshotListener
                }

                val organizationId = profileSnap?.getString("organizationId")
                val obrasQuery = if (!organizationId.isNullOrEmpty()) {
                    firestore.collection("obras").whereEqualTo("organizationId", organizationId)
                } else {
                    firestore.collection("obras").whereEqualTo("userId", userId)
                }
                
                obrasRegistration?.remove()
                
                obrasRegistration = obrasQuery.addSnapshotListener { obrasSnap, obrasError ->
                    if (obrasError != null) {
                        trySend(Resource.Error(obrasError.message ?: "Error al cargar obras archivadas"))
                        return@addSnapshotListener
                    }

                    if (obrasSnap != null) {
                        try {
                            val obrasDocs = obrasSnap.toObjects(ObraDocument::class.java)
                            val obrasList = obrasDocs.filter { it.isArchived }.map { it.toDomain() }
                            trySend(Resource.Success(obrasList))
                        } catch (e: Exception) {
                            trySend(Resource.Error("Error de datos: ${e.message}"))
                        }
                    }
                }
            }
            awaitClose { 
                registration.remove()
                obrasRegistration?.remove()
            }
        }
    }    
        
    override suspend fun archiveObra(obraId: String): Result<Unit> = try {
        firestore.collection("obras").document(obraId).update(
            mapOf("isArchived" to true, "lastActivity" to FieldValue.serverTimestamp())
        )
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteObra(obraId: String): Result<Unit> = try {
        firestore.collection("obras").document(obraId).delete()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun saveObra(obra: Obra): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("No auth")
        val userSnapshot = firestore.collection("users").document(userId).get().await()
        val organizationId = userSnapshot.getString("organizationId") ?: "" 
        val obraDoc = obra.toDocument().copy(
            userId = userId, 
            organizationId = organizationId,
            fechaCreacion = Date(),
            lastActivity = Date()
        )
        firestore.collection("obras").document(obraDoc.id.ifEmpty { UUID.randomUUID().toString() }).set(obraDoc)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override fun getObraById(obraId: String): Flow<Obra> = callbackFlow {
        val listener = firestore.collection("obras").document(obraId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val obraDoc = snapshot.toObject(ObraDocument::class.java)
                if (obraDoc != null) trySend(obraDoc.toDomain()).isSuccess
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun updateObra(obra: Obra): Result<Unit> = try {
        val obraDoc = obra.toDocument()
        firestore.collection("obras").document(obra.id).update(
            mapOf(
                "nombreObra" to obraDoc.nombreObra,
                "descripcion" to obraDoc.descripcion,
                "estado" to obraDoc.estado,
                "telefono" to obraDoc.telefono,
                "direccion" to obraDoc.direccion,
                "clienteId" to obraDoc.clienteId,
                "clienteNombre" to obraDoc.clienteNombre,
                "lastActivity" to FieldValue.serverTimestamp()
            )
        )
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override fun getTareas(obraId: String): Flow<List<Tarea>> = callbackFlow {
        val listener = firestore.collection("obras").document(obraId).collection("tareas")
            .orderBy("fechaCreacion")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val docs = snapshot.documents.mapNotNull { it.toObject(TareaDocument::class.java) }
                    trySend(docs.map { it.toDomain() }).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addTarea(obraId: String, tarea: Tarea): Result<Unit> = try {
        val obraRef = firestore.collection("obras").document(obraId)
        val tareasCollection = obraRef.collection("tareas")
        val tareaRef = tareasCollection.document()
        
        val batch = firestore.batch()
        batch.set(tareaRef, tarea.toDocument().copy(id = tareaRef.id, fechaCreacion = Date()))
        batch.update(obraRef, mapOf(
            "tareasTotales" to FieldValue.increment(1),
            "lastActivity" to FieldValue.serverTimestamp()
        ))
        batch.commit()
        
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateTareaStatus(obraId: String, tareaId: String, completada: Boolean): Result<Unit> = try {
        val obraRef = firestore.collection("obras").document(obraId)
        val tareaRef = obraRef.collection("tareas").document(tareaId)
        
        val batch = firestore.batch()
        batch.update(tareaRef, "completada", completada)
        
        val increment = if (completada) 1L else -1L
        batch.update(obraRef, mapOf(
            "tareasCompletadas" to FieldValue.increment(increment),
            "lastActivity" to FieldValue.serverTimestamp()
        ))
        
        batch.commit()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteTarea(obraId: String, tareaId: String): Result<Unit> = try {
        val obraRef = firestore.collection("obras").document(obraId)
        val tareaRef = obraRef.collection("tareas").document(tareaId)
        
        // Obtenemos el estado de la tarea antes de borrarla para ajustar contadores
        // Nota: Aquí sí necesitamos un await para saber qué contador decrementar
        val snapshot = tareaRef.get().await()
        val fueCompletada = snapshot.getBoolean("completada") ?: false
        
        val batch = firestore.batch()
        batch.delete(tareaRef)
        batch.update(obraRef, mapOf(
            "tareasTotales" to FieldValue.increment(-1),
            "tareasCompletadas" to if (fueCompletada) FieldValue.increment(-1) else FieldValue.increment(0),
            "lastActivity" to FieldValue.serverTimestamp()
        ))
        batch.commit()
        
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override fun getAvances(obraId: String): Flow<List<Avance>> = callbackFlow {
        val listener = firestore.collection("obras").document(obraId).collection("avances")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val docs = snapshot.documents.mapNotNull { it.toObject(AvanceDocument::class.java) }
                    trySend(docs.map { it.toDomain() }).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addAvance(obraId: String, avance: Avance): Result<Unit> = try {
        val obraRef = firestore.collection("obras").document(obraId)
        val batch = firestore.batch()
        val avanceRef = obraRef.collection("avances").document()
        
        batch.set(avanceRef, avance.toDocument())
        batch.update(obraRef, "lastActivity", FieldValue.serverTimestamp())
        batch.commit()
        
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteAvance(obraId: String, avanceId: String): Result<Unit> = try {
        firestore.collection("obras").document(obraId).collection("avances").document(avanceId).delete()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override fun getMovimientos(obraId: String): Flow<List<Movimiento>> = callbackFlow {
        val listener = firestore.collection("obras").document(obraId).collection("movimientos")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val docs = snapshot.documents.mapNotNull { it.toObject(MovimientoDocument::class.java) }
                    trySend(docs.map { it.toDomain() }).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addMovimiento(obraId: String, movimiento: Movimiento): Result<Unit> = try {
        val obraRef = firestore.collection("obras").document(obraId)
        val batch = firestore.batch()
        val movRef = obraRef.collection("movimientos").document()
        
        batch.set(movRef, movimiento.toDocument())
        batch.update(obraRef, "lastActivity", FieldValue.serverTimestamp())
        batch.commit()
        
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteMovimiento(obraId: String, movimientoId: String): Result<Unit> = try {
        firestore.collection("obras").document(obraId).collection("movimientos").document(movimientoId).delete()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override fun getPresupuestoItems(obraId: String): Flow<List<PresupuestoItem>> = callbackFlow {
        val listener = firestore.collection("obras").document(obraId).collection("presupuesto_items")
            .orderBy("fechaCreacion", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val docs = snapshot.documents.mapNotNull { it.toObject(PresupuestoItemDocument::class.java) }
                    trySend(docs.map { it.toDomain() }).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addPresupuestoItem(obraId: String, item: PresupuestoItem): Result<Unit> = try {
        firestore.collection("obras").document(obraId).collection("presupuesto_items").add(item.toDocument())
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updatePresupuestoItem(obraId: String, item: PresupuestoItem): Result<Unit> = try {
        firestore.collection("obras").document(obraId).collection("presupuesto_items").document(item.id).set(item.toDocument())
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deletePresupuestoItem(obraId: String, itemId: String): Result<Unit> = try {
        firestore.collection("obras").document(obraId).collection("presupuesto_items").document(itemId).delete()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateItemLogistics(obraId: String, itemId: String, isComprado: Boolean, isInstalado: Boolean, costoReal: Double?): Result<Unit> = try {
        firestore.collection("obras").document(obraId).collection("presupuesto_items").document(itemId)
            .update(mapOf("isComprado" to isComprado, "isInstalado" to isInstalado, "costoReal" to costoReal))
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}