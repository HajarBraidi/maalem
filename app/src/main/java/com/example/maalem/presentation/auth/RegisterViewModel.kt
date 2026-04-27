package com.example.maalem.presentation.auth

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maalem.data.model.Artisan
import com.example.maalem.data.model.Citizen
import com.example.maalem.data.model.User
import com.example.maalem.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val state: StateFlow<RegisterState> = _state

    // Stocke temporairement la photo CIN choisie
    private var cinUri: Uri? = null

    fun setCinUri(uri: Uri) {
        cinUri = uri
    }

    fun hasCinPhoto(): Boolean = cinUri != null

    // Inscription Citoyen
    fun registerCitizen(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        address: String
    ) = viewModelScope.launch {
        _state.value = RegisterState.Loading
        val citizen = Citizen(
            name = name,
            email = email,
            phone = phone,
            address = address
        )
        registerUseCase(email, password, confirmPassword, citizen).fold(
            onSuccess = { _state.value = RegisterState.Success },
            onFailure = { _state.value = RegisterState.Error(it.message ?: "Erreur") }
        )
    }

    // Inscription Artisan avec CIN
    fun registerArtisan(
        context: Context,
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        specialty: String,
        city: String,
        bio: String
    ) = viewModelScope.launch {
        _state.value = RegisterState.Loading

        val uri = cinUri
        if (uri == null) {
            _state.value = RegisterState.Error("Photo CIN requise")
            return@launch
        }

        try {
            // Convertir la photo CIN en Base64
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            val resized = Bitmap.createScaledBitmap(bitmap, 800, 800, true)
            val baos = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val cinBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

            val artisan = Artisan(
                name = name,
                email = email,
                phone = phone,
                specialty = specialty,
                city = city,
                bio = bio,
                isActive = false,
                isValidated = false,
                cinPhotoBase64 = "data:image/jpeg;base64,$cinBase64"
            )

            registerUseCase(email, password, confirmPassword, artisan).fold(
                onSuccess = { _state.value = RegisterState.Success },
                onFailure = { _state.value = RegisterState.Error(it.message ?: "Erreur") }
            )
        } catch (e: Exception) {
            _state.value = RegisterState.Error("Erreur lecture photo: ${e.message}")
        }
    }
}