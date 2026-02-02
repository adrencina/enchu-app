package com.adrencina.enchu.domain.use_case

import android.net.Uri
import com.adrencina.enchu.domain.model.Avance
import com.adrencina.enchu.domain.repository.ObraRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class SaveAvanceUseCase @Inject constructor(
    private val repository: ObraRepository,
    private val storage: FirebaseStorage
) {
    suspend operator fun invoke(obraId: String, descripcion: String, imageUris: List<Uri>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uploadedUrls = imageUris.map { uri ->
                async {
                    val filename = "${UUID.randomUUID()}.jpg"
                    val ref = storage.reference.child("obras/$obraId/avances/$filename")
                    ref.putFile(uri).await()
                    ref.downloadUrl.await().toString()
                }
            }.awaitAll()

            val avance = Avance(
                descripcion = descripcion,
                fotosUrls = uploadedUrls
            )

            repository.addAvance(obraId, avance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
