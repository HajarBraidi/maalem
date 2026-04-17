//envoyer les états (loading, succès, erreur) à l’écran
package com.example.maalem.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maalem.data.model.UserRole
import com.example.maalem.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


//LoginState = ce que l’écran doit afficher
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val role: UserRole) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun login(email: String, password: String) = viewModelScope.launch {
        _state.value = LoginState.Loading
        loginUseCase(email, password).fold(
            onSuccess = { role -> _state.value = LoginState.Success(role) },
            onFailure = { e -> _state.value = LoginState.Error(e.message ?: "Erreur inconnue") }
        )
    }
}

//Ce ViewModel fait :
//
//reçoit email + password
//met état = Loading
//appelle UseCase
//retourne :
//Success(role)
//Error(message)