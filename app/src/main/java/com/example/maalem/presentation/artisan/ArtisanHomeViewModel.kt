package com.example.maalem.presentation.artisan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maalem.data.model.Artisan
import com.example.maalem.data.model.Offer
import com.example.maalem.data.model.Request
import com.example.maalem.domain.repository.ArtisanRepository
import com.example.maalem.domain.usecase.SendOfferUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Les différents états possibles de l'écran artisan
sealed class ArtisanUiState {
    object Idle : ArtisanUiState()
    object Loading : ArtisanUiState()
    data class RequestsLoaded(val requests: List<Request>) : ArtisanUiState()
    data class OffersLoaded(val offers: List<Offer>) : ArtisanUiState()
    data class ProfileLoaded(val artisan: Artisan) : ArtisanUiState()
    object OfferSent : ArtisanUiState()
    data class Error(val message: String) : ArtisanUiState()
}

@HiltViewModel
class ArtisanHomeViewModel @Inject constructor(
    private val artisanRepository: ArtisanRepository,
    private val sendOfferUseCase: SendOfferUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow<ArtisanUiState>(ArtisanUiState.Idle)
    val state: StateFlow<ArtisanUiState> = _state

    // Charger les demandes disponibles pour l'artisan connecté
    fun loadAvailableRequests() = viewModelScope.launch {
        _state.value = ArtisanUiState.Loading
        val uid = auth.currentUser?.uid ?: run {
            _state.value = ArtisanUiState.Error("Non authentifié")
            return@launch
        }

        // D'abord, récupérer la spécialité de l'artisan
        artisanRepository.getArtisanProfile(uid).fold(
            onSuccess = { artisan ->
                // Puis charger les requests de cette spécialité
                artisanRepository.getAvailableRequests(artisan.specialty).fold(
                    onSuccess = { _state.value = ArtisanUiState.RequestsLoaded(it) },
                    onFailure = { _state.value = ArtisanUiState.Error(it.message ?: "Erreur") }
                )
            },
            onFailure = { _state.value = ArtisanUiState.Error(it.message ?: "Erreur profil") }
        )
    }

    // Envoyer une offre sur une demande
    fun sendOffer(
        requestId: String,
        price: Double,
        delay: String,
        message: String,
        artisanName: String,
        artisanPhone: String
    ) = viewModelScope.launch {
        _state.value = ArtisanUiState.Loading
        val uid = auth.currentUser?.uid ?: run {
            _state.value = ArtisanUiState.Error("Non authentifié")
            return@launch
        }
        val offer = Offer(
            requestId = requestId,
            artisanId = uid,
            artisanName = artisanName,
            artisanPhone = artisanPhone,
            price = price,
            delay = delay,
            message = message
        )
        sendOfferUseCase(offer).fold(
            onSuccess = { _state.value = ArtisanUiState.OfferSent },
            onFailure = { _state.value = ArtisanUiState.Error(it.message ?: "Erreur") }
        )
    }

    // Charger mes offres envoyées
    fun loadMyOffers() = viewModelScope.launch {
        _state.value = ArtisanUiState.Loading
        val uid = auth.currentUser?.uid ?: run {
            _state.value = ArtisanUiState.Error("Non authentifié")
            return@launch
        }
        artisanRepository.getMyOffers(uid).fold(
            onSuccess = { _state.value = ArtisanUiState.OffersLoaded(it) },
            onFailure = { _state.value = ArtisanUiState.Error(it.message ?: "Erreur") }
        )
    }

    // Charger le profil artisan
    fun loadProfile() = viewModelScope.launch {
        _state.value = ArtisanUiState.Loading
        val uid = auth.currentUser?.uid ?: run {
            _state.value = ArtisanUiState.Error("Non authentifié")
            return@launch
        }
        artisanRepository.getArtisanProfile(uid).fold(
            onSuccess = { _state.value = ArtisanUiState.ProfileLoaded(it) },
            onFailure = { _state.value = ArtisanUiState.Error(it.message ?: "Erreur") }
        )
    }
}