package com.adrencina.enchu.data.repository

import com.adrencina.enchu.data.model.Obra
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Esta es la implementación real de nuestro Repositorio.
 * Hilt se encargará de proveer las instancias de Firestore y Auth.
 * @Inject constructor() le dice a Hilt: "Sé cómo construir esta clase".
 */
class ObraRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ObraRepository {

    // ... (la función getObras de antes se borra y se reemplaza por esta)
    override fun getObras(): Flow<List<Obra>> {
        val userId = auth.currentUser?.uid ?: return flowOf(emptyList())

        // Usamos callbackFlow para convertir el listener de Firebase en un Flow de Kotlin
        return callbackFlow {
            val listener = firestore.collection("obras")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Si hay un error, cerramos el flow con la excepción
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        // Mapeamos el resultado a nuestra lista de data class
                        // toObjects() con @DocumentId ya se encarga de poner el ID
                        val obras = snapshot.toObjects(Obra::class.java)
                        // Enviamos la nueva lista a través del flow
                        trySend(obras).isSuccess
                    }
                }
            // Esto se ejecuta cuando el flow se cancela, para limpiar el listener
            awaitClose { listener.remove() }
        }
    }
}