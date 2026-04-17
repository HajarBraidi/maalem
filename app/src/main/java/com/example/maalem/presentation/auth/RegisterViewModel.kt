package com.example.maalem.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maalem.data.model.*
import com.example.maalem.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

    fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        role: UserRole,
        // Champs spécifiques artisan
        specialty: String = "",
        city: String = "",
        bio: String = "",
        // Champs spécifiques citoyen
        address: String = ""
    ) = viewModelScope.launch {
        _state.value = RegisterState.Loading

        val user: User = when (role) {
            UserRole.CITIZEN -> Citizen(
                name = name, email = email, phone = phone, address = address
            )
            UserRole.ARTISAN -> Artisan(
                name = name, email = email, phone = phone,
                specialty = specialty, city = city, bio = bio
            )
            UserRole.ADMIN -> Admin(
                name = name, email = email, phone = phone
            )
        }

        registerUseCase(email, password, confirmPassword, user).fold(
            onSuccess = { _state.value = RegisterState.Success },
            onFailure = { e -> _state.value = RegisterState.Error(e.message ?: "Erreur inconnue") }
        )
    }
}