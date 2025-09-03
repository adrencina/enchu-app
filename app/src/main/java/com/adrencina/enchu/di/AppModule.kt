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

/**
 * Este es un Módulo de Hilt. Es como un manual de instrucciones para Hilt.
 * Le dice cómo crear y proveer las dependencias que nuestra app necesita.
 * @Module: Le dice a Hilt que esta clase contiene instrucciones de provisión.
 * @InstallIn(SingletonComponent::class): Le dice que estas instrucciones son para
 * toda la vida de la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Provee la instancia de FirebaseAuth
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    // Provee la instancia de FirebaseFirestore
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // Instrucción CLAVE: Cuando alguien en la app pida un "ObraRepository" (la interfaz),
    // Hilt sabrá que debe crear y entregar una instancia de "ObraRepositoryImpl" (la implementación).
    @Provides
    @Singleton
    fun provideObraRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): ObraRepository {
        return ObraRepositoryImpl(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository {
        return AuthRepositoryImpl(auth)
    }
}