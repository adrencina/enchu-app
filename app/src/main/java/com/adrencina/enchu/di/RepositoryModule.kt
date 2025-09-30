package com.adrencina.enchu.di

import com.adrencina.enchu.data.repository.ClienteRepository
import com.adrencina.enchu.data.repository.ClienteRepositoryImpl
import com.adrencina.enchu.data.repository.ObraRepository
import com.adrencina.enchu.data.repository.ObraRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindObraRepository(
        impl: ObraRepositoryImpl
    ): ObraRepository

    @Binds
    abstract fun bindClienteRepository(
        impl: ClienteRepositoryImpl
    ): ClienteRepository
}