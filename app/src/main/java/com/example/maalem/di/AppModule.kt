package com.example.maalem.di

import com.example.maalem.data.repository.AuthRepositoryImpl
import com.example.maalem.data.repository.CitizenRepositoryImpl
import com.example.maalem.domain.repository.AuthRepository
import com.example.maalem.domain.repository.CitizenRepository
import com.example.maalem.data.repository.ArtisanRepositoryImpl
import com.example.maalem.domain.repository.ArtisanRepository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
    //  Bien à l'intérieur de FirebaseModule
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCitizenRepository(impl: CitizenRepositoryImpl): CitizenRepository
    // CitizenRepository ici, pas besoin de CitizenRepositoryModule séparé

    @Binds
    @Singleton
    abstract fun bindArtisanRepository(impl: ArtisanRepositoryImpl): ArtisanRepository
}
