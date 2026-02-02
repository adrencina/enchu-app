package com.adrencina.enchu.domain.model

enum class EstadoObra(val value: String) {
    PRESUPUESTADO("Presupuestado"),
    EN_PROGRESO("En Progreso"),
    EN_PAUSA("En Pausa"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado"),
    DESCONOCIDO("Desconocido");

    companion object {
        fun fromValue(value: String?): EstadoObra {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: DESCONOCIDO
        }
    }
}