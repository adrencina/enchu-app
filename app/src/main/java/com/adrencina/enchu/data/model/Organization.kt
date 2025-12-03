package com.adrencina.enchu.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Organization(
    @DocumentId val id: String = "",
    val name: String = "", // Nombre de la empresa (ej. ElectroJuan)
    val ownerId: String = "", 
    val members: List<String> = emptyList(),
    
    // Branding & Contacto Comercial
    val logoUrl: String = "",
    val businessPhone: String = "",
    val businessEmail: String = "",
    val businessAddress: String = "",
    val businessWeb: String = "",

    @ServerTimestamp
    val createdAt: Date? = null
)
