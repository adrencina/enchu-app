package com.adrencina.enchu.data.repository

import com.adrencina.enchu.data.model.Obra
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Esta es la implementación real de nuestro Repositorio.
 * Hilt se encargará de proveer las instancias de Firestore y Auth.
 * @Inject constructor() le dice a Hilt: "Sé cómo construir esta clase".
 */
class ObraRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ObraRepository {

    // Obtenemos el ID del usuario actual. Es importante que el usuario esté logueado
    // al llamar a las funciones del repositorio.
    private val userId: String
        get() = auth.currentUser?.uid.orEmpty()


    override fun getObras(): Flow<List<Obra>> {
        // Hacemos una consulta a la colección "obras" de Firestore,
        // filtrando solo los documentos cuyo "userId" coincida con el del usuario actual.
        return firestore.collection("obras")
            .whereEqualTo("userId", userId)
            .snapshots() // "snapshots()" nos da actualizaciones en tiempo real
            .map { snapshot ->
                snapshot.toObjects(Obra::class.java)
            }
    }
}