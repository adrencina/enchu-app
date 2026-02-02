package com.adrencina.enchu.domain.model

import java.util.Date

data class Avance(
    val id: String = "",
    val descripcion: String = "",
    val fotosUrls: List<String> = emptyList(),
    val fecha: Date? = null
)