package com.adrencina.enchu.data.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun firebaseSignInWithGoogle(token: String): Boolean
    fun signOut()
}