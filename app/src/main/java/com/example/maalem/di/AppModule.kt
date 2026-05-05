package com.example.maalem.di

// ✅ Imports fusionnés des deux branches
import com.example.maalem.data.repository.AdminRepositoryImpl
import com.example.maalem.data.repository.ArtisanRepositoryImpl
import com.example.maalem.data.repository.AuthRepositoryImpl
import com.example.maalem.data.repository.ChatRepositoryImpl
import com.example.maalem.data.repository.CitizenRepositoryImpl
import com.example.maalem.domain.repository.AdminRepository
import com.example.maalem.domain.repository.ArtisanRepository
import com.example.maalem.domain.repository.AuthRepository
import com.example.maalem.domain.repository.ChatRepository
import com.example.maalem.domain.repository.CitizenRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ✅ Module Firebase (providers)
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
}

// ✅ Module Repositories (tous les binds ensemble)
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // Auth
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    // Citoyen
    @Binds
    @Singleton
    abstract fun bindCitizenRepository(
        impl: CitizenRepositoryImpl
    ): CitizenRepository

    // Artisan (Khadija)
    @Binds
    @Singleton
    abstract fun bindArtisanRepository(
        impl: ArtisanRepositoryImpl
    ): ArtisanRepository

    // Admin (Hajar)
    @Binds
    @Singleton
    abstract fun bindAdminRepository(
        impl: AdminRepositoryImpl
    ): AdminRepository

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class ChatRepositoryModule {
        @Binds
        @Singleton
        abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository
    }

}