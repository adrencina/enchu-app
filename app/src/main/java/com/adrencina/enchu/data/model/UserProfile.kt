package com.adrencina.enchu.data.model

import com.google.firebase.firestore.DocumentId

data class UserProfile(
    @DocumentId val id: String = "", // Mismo que Firebase Auth UID
    val email: String = "",
    val displayName: String = "",
    val organizationId: String = "", // Link a la organización
    val role: String = "OWNER" // OWNER, EMPLOYEE
)
