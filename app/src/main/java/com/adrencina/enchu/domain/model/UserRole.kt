package com.adrencina.enchu.domain.model

enum class UserRole(val value: String) {
    OWNER("Owner"),
    ADMIN("Admin"),
    WORKER("Worker"),
    GUEST("Guest");

    companion object {
        fun fromValue(value: String): UserRole {
            return entries.find { it.value == value } ?: WORKER
        }
    }
    
    fun canViewMoney(): Boolean = this == OWNER || this == ADMIN
    fun canEditObra(): Boolean = this == OWNER || this == ADMIN
    fun canManageTeam(): Boolean = this == OWNER
}
