package com.adrencina.enchu.data.repository

import android.net.Uri
import com.adrencina.enchu.data.mapper.toDocument
import com.adrencina.enchu.data.mapper.toDomain
import com.adrencina.enchu.data.model.OrganizationDocument
import com.adrencina.enchu.domain.model.Organization
import com.adrencina.enchu.domain.repository.OrganizationRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OrganizationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : OrganizationRepository {

    override fun getOrganization(orgId: String): Flow<Organization?> = callbackFlow {
        if (orgId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("organizations").document(orgId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val doc = snapshot.toObject(OrganizationDocument::class.java)
                        trySend(doc?.toDomain())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateOrganization(organization: Organization): Result<Unit> {
        return try {
            val doc = organization.toDocument()
            firestore.collection("organizations").document(organization.id).set(doc).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadLogo(orgId: String, uri: Uri): Result<String> {
        return try {
            val storageRef = storage.reference.child("organizations/$orgId/logo.jpg")
            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun incrementStorageUsed(orgId: String, bytes: Long): Result<Unit> {
        return try {
            firestore.collection("organizations").document(orgId)
                .update("storageUsed", FieldValue.increment(bytes))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}