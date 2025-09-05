package com.adrencina.enchu.di

import com.adrencina.enchu.data.repository.AuthRepository
import com.adrencina.enchu.data.repository.AuthRepositoryImpl
import com.adrencina.enchu.data.repository.ObraRepository
import com.adrencina.enchu.data.repository.ObraRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository {
        // CORRECCIÓN: Ahora solo le pasamos 'auth', que es lo que el constructor de AuthRepositoryImpl espera.
        return AuthRepositoryImpl(auth)
    }

    @Provides
    @Singleton
    fun provideObraRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): ObraRepository {
        // CORRECCIÓN: Invertimos el orden de los argumentos para que coincida
        // con el constructor de ObraRepositoryImpl.
        return ObraRepositoryImpl(auth, firestore)
    }
}