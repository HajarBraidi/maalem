package com.example.maalem.presentation.citizen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maalem.data.model.Artisan
import com.example.maalem.data.model.Request
import com.example.maalem.domain.repository.CitizenRepository
import com.example.maalem.domain.usecase.CreateRequestUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CitizenUiState {
    object Idle : CitizenUiState()
    object Loading : CitizenUiState()
    data class ArtisansLoaded(val artisans: List<Artisan>) : CitizenUiState()
    object RequestSent : CitizenUiState()
    data class Error(val message: String) : CitizenUiState()
}

@HiltViewModel
class CitizenHomeViewModel @Inject constructor(
    private val citizenRepository: CitizenRepository,
    private val createRequestUseCase: CreateRequestUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow<CitizenUiState>(CitizenUiState.Idle)
    val state: StateFlow<CitizenUiState> = _state

    init {
        loadArtisans()
    }

    fun loadArtisans(category: String? = null) = viewModelScope.launch {
        _state.value = CitizenUiState.Loading
        citizenRepository.getArtisans(category).fold(
            onSuccess = { _state.value = CitizenUiState.ArtisansLoaded(it) },
            onFailure = { _state.value = CitizenUiState.Error(it.message ?: "Erreur") }
        )
    }

    fun createRequest(
        title: String,
        description: String,
        category: String,
        city: String,
        citizenName: String
    ) = viewModelScope.launch {
        _state.value = CitizenUiState.Loading
        val request = Request(
            citizenId = auth.currentUser?.uid ?: "",
            citizenName = citizenName,
            title = title,
            description = description,
            category = category,
            city = city
        )
        createRequestUseCase(request).fold(
            onSuccess = { _state.value = CitizenUiState.RequestSent },
            onFailure = { _state.value = CitizenUiState.Error(it.message ?: "Erreur") }
        )
    }
}