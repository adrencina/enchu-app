package com.adrencina.enchu.data.model

import com.google.firebase.firestore.DocumentId

data class UserProfile(
    @DocumentId val id: String = "", // Mismo que Firebase Auth UID
    val email: String = "",
    val displayName: String = "",
    val organizationId: String = "", // Link a la organización
    val role: String = "OWNER", // OWNER, WORKER, ADMIN
    val status: String = "ACTIVE", // PENDING, ACTIVE, SUSPENDED
    val lastRejectionTimestamp: Long = 0L
)
