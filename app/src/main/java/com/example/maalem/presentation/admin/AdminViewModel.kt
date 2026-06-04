package com.example.maalem.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maalem.data.model.AppLocation
import com.example.maalem.data.model.AppStats
import com.example.maalem.data.model.Artisan
import com.example.maalem.data.model.Citizen
import com.example.maalem.domain.repository.AdminRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminUiState {
    object Idle : AdminUiState()
    object Loading : AdminUiState()
    object ActionSuccess : AdminUiState()
    data class PendingArtisansLoaded(val artisans: List<Artisan>) : AdminUiState()
    data class ArtisansLoaded(val artisans: List<Artisan>) : AdminUiState()
    data class CitizensLoaded(val citizens: List<Citizen>) : AdminUiState()
    data class StatsLoaded(val stats: AppStats) : AdminUiState()
    data class CategoriesLoaded(val categories: List<String>) : AdminUiState()
    data class Error(val message: String) : AdminUiState()

    // États supplémentaires dans AdminUiState
    data class LocationsLoaded(val locations: List<AppLocation>) : AdminUiState()
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val state: StateFlow<AdminUiState> = _state

    // --- Artisans en attente ---
    fun loadPendingArtisans() = viewModelScope.launch {
        _state.value = AdminUiState.Loading
        adminRepository.getPendingArtisans().fold(
            onSuccess = { _state.value = AdminUiState.PendingArtisansLoaded(it) },
            onFailure = { _state.value = AdminUiState.Error(it.message ?: "Erreur") }
        )
    }

    fun validateArtisan(uid: String, approve: Boolean) = viewModelScope.launch {
        _state.value = AdminUiState.Loading
        adminRepository.validateArtisan(uid, approve).fold(
            onSuccess = {
                _state.value = AdminUiState.ActionSuccess
                loadPendingArtisans()
            },
            onFailure = { _state.value = AdminUiState.Error(it.message ?: "Erreur") }
        )
    }

    // --- Gestion comptes ---
    fun loadAllArtisans() = viewModelScope.launch {
        _state.value = AdminUiState.Loading
        adminRepository.getAllArtisans().fold(
            onSuccess = { _state.value = AdminUiState.ArtisansLoaded(it) },
            onFailure = { _state.value = AdminUiState.Error(it.message ?: "Erreur") }
        )
    }

    fun loadAllCitizens() = viewModelScope.launch {
        _state.value = AdminUiState.Loading
        adminRepository.getAllCitizens().fold(
            onSuccess = { _state.value = AdminUiState.CitizensLoaded(it) },
            onFailure = { _state.value = AdminUiState.Error(it.message ?: "Erreur") }
        )
    }

    fun toggleUser(uid: String, isActive: Boolean) = viewModelScope.launch {
        adminRepository.toggleUserAccount(uid, isActive).fold(
            onSuccess = { _state.value = AdminUiState.ActionSuccess },
            onFailure = { _state.value = AdminUiState.Error(it.message ?: "Erreur") }
        )
    }

    // --- Statistiques ---
    fun loadStats() = viewModelScope.launch {
        _state.value = AdminUiState.Loading
        adminRepository.getStats().fold(
            onSuccess = { _state.value = AdminUiState.StatsLoaded(it) },
            onFailure = { _state.value = AdminUiState.Error(it.message ?: "Erreur") }
        )
    }

    // --- Catégories ---
    fun loadCategories() = viewModelScope.launch {
        _state.value = AdminUiState.Loading
        adminRepository.getCategories().fold(
            onSuccess = { _state.value = AdminUiState.CategoriesLoaded(it) },
            onFailure = { _state.value = AdminUiState.Error(it.message ?: "Erreur") }
        )
    }

    fun addCategory(category: String) = viewModelScope.launch {
        adminRepository.addCategory(category).fold(
            onSuccess = { loadCategories() },
            onFailure = { _state.value = AdminUiState.Error(it.message ?: "Erreur") }
        )
    }

    fun deleteCategory(category: String) = viewModelScope.launch {
        adminRepository.deleteCategory(category).fold(
            onSuccess = { loadCategories() },
            onFailure = { _state.value = AdminUiState.Error(it.message ?: "Erreur") }
        )
    }

    // --- Notifications ---
    fun sendNotification(title: String, message: String) = viewModelScope.launch {
        if (title.isBlank() || message.isBlank()) {
            _state.value = AdminUiState.Error("Titre et message requis")
            return@launch
        }
        _state.value = AdminUiState.Loading
        adminRepository.sendNotification(title, message).fold(
            onSuccess = { _state.value = AdminUiState.ActionSuccess },
            onFailure = { _state.value = AdminUiState.Error(it.message ?: "Erreur") }
        )
    }

    // Fonctions
    fun loadLocations() = viewModelScope.launch {
        _state.value = AdminUiState.Loading
        adminRepository.getLocations().fold(
            onSuccess = { _state.value = AdminUiState.LocationsLoaded(it) },
            onFailure = { _state.value = AdminUiState.Error(it.message ?: "Erreur") }
        )
    }

    fun addLocation(name: String, latitude: Double, longitude: Double) = viewModelScope.launch {
        if (name.isBlank()) {
            _state.value = AdminUiState.Error("Nom requis")
            return@launch
        }
        val location = AppLocation(name = name, latitude = latitude, longitude = longitude)
        adminRepository.addLocation(location).fold(
            onSuccess = { loadLocations() },
            onFailure = { _state.value = AdminUiState.Error(it.message ?: "Erreur") }
        )
    }

    fun deleteLocation(locationId: String) = viewModelScope.launch {
        adminRepository.deleteLocation(locationId).fold(
            onSuccess = { loadLocations() },
            onFailure = { _state.value = AdminUiState.Error(it.message ?: "Erreur") }
        )
    }

    fun logout() = auth.signOut()
}