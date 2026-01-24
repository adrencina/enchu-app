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
    val cuit: String = "", // CUIT de la empresa usuaria
    val taxCondition: String = "", // Condición fiscal (Resp. Inscripto, Monotributo, etc.)
    val lastBudgetNumber: Int = 0, // Último número de presupuesto emitido globalmente
    val plan: String = "FREE", // FREE, PRO
    val storageUsed: Long = 0, // Bytes usados en total

    @ServerTimestamp
    val createdAt: Date? = null
)
