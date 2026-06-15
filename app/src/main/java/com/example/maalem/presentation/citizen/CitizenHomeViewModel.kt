package com.example.maalem.presentation.citizen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maalem.data.model.Artisan
import com.example.maalem.data.model.Citizen
import com.example.maalem.data.model.Offer
import com.example.maalem.data.model.Request
import com.example.maalem.domain.repository.CitizenRepository
import com.example.maalem.domain.usecase.CreateRequestUseCase
import com.example.maalem.utils.LocationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.util.Log
import com.example.maalem.domain.repository.ReviewRepository

data class NearbyArtisan(
    val artisan: Artisan,
    val distanceKm: Double
)

sealed class CitizenUiState {
    object Idle : CitizenUiState()
    object Loading : CitizenUiState()

    data class HomeLoaded(
        val citizen: Citizen,
        val artisans: List<Artisan>,
        val nearestArtisans: List<NearbyArtisan>,
        val selectedArtisan: Artisan? = null
    ) : CitizenUiState()

    data class ArtisansLoaded(val artisans: List<Artisan>) : CitizenUiState()
    data class RequestsLoaded(val requests: List<Request>) : CitizenUiState()
    data class OffersLoaded(val offers: List<Offer>) : CitizenUiState()
    data class ArtisanLoaded(val artisan: Artisan) : CitizenUiState()

    object RequestSent : CitizenUiState()
    object OfferAccepted : CitizenUiState()
    object OfferRejected : CitizenUiState()

    data class Error(val message: String) : CitizenUiState()

    // ★ NOUVEAU — dans sealed class CitizenUiState
    data class ReviewsLoaded(val reviews: List<com.example.maalem.data.model.Review>) : CitizenUiState()
    object ReviewSubmitted : CitizenUiState()
    data class AlreadyReviewed(val artisanId: String) : CitizenUiState()
}

@HiltViewModel
class CitizenHomeViewModel @Inject constructor(
    private val citizenRepository: CitizenRepository,
    private val createRequestUseCase: CreateRequestUseCase,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CitizenUiState>(CitizenUiState.Idle)
    val state: StateFlow<CitizenUiState> = _state

    val currentUserId get() = auth.currentUser?.uid ?: ""

    private var currentCitizen: Citizen? = null
    private var currentArtisans: List<Artisan> = emptyList()
    private var currentNearestArtisans: List<NearbyArtisan> = emptyList()
    private var currentCategory: String? = null

    init {
        loadHome(null)
    }

    fun loadHome(category: String? = null) = viewModelScope.launch {
        _state.value = CitizenUiState.Loading
        currentCategory = category

        try {
            val uid = currentUserId
            if (uid.isBlank()) {
                _state.value = CitizenUiState.Error("Utilisateur non connecté")
                return@launch
            }

            val citizenDoc = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val citizen = citizenDoc.toObject(Citizen::class.java)

            if (citizen == null) {
                _state.value = CitizenUiState.Error("Impossible de charger le citoyen")
                return@launch
            }

            if (citizen.latitude == 0.0 && citizen.longitude == 0.0) {
                _state.value = CitizenUiState.Error("Votre localisation est invalide")
                return@launch
            }

            citizenRepository.getArtisans(category).fold(
                onSuccess = { artisans ->

                    Log.d("CitizenHomeVM", "Artisans reçus = ${artisans.size}")

                    artisans.forEach {
                        Log.d(
                            "CitizenHomeVM",
                            "artisan=${it.name}, active=${it.isActive}, validated=${it.isValidated}, lat=${it.latitude}, lng=${it.longitude}, specialty=${it.specialty}"
                        )
                    }

                    currentCitizen = citizen

                    val validArtisans = artisans.filter { artisan ->
                        artisan.isValidated &&
                                artisan.isActive &&
                                !(artisan.latitude == 0.0 && artisan.longitude == 0.0)
                    }

                    Log.d("CitizenHomeVM", "Artisans valides = ${validArtisans.size}")
                    Log.d("CitizenHomeVM", "Artisans proches = ${currentNearestArtisans.size}")

                    currentArtisans = validArtisans

                    currentNearestArtisans = validArtisans
                        .map { artisan ->
                            val distance = LocationUtils.distanceInKm(
                                citizen.latitude,
                                citizen.longitude,
                                artisan.latitude,
                                artisan.longitude
                            )

                            NearbyArtisan(
                                artisan = artisan,
                                distanceKm = distance
                            )
                        }
                        .sortedBy { it.distanceKm }
                        .take(5)

                    _state.value = CitizenUiState.HomeLoaded(
                        citizen = citizen,
                        artisans = validArtisans,
                        nearestArtisans = currentNearestArtisans,
                        selectedArtisan = null
                    )
                },
                onFailure = {
                    _state.value = CitizenUiState.Error(it.message ?: "Erreur")
                }
            )

        } catch (e: Exception) {
            _state.value = CitizenUiState.Error(e.message ?: "Erreur")
        }
    }

    fun selectArtisan(artisan: Artisan) {
        val citizen = currentCitizen ?: return

        val reorderedList = listOf(artisan) + currentArtisans.filter { it.uid != artisan.uid }

        _state.value = CitizenUiState.HomeLoaded(
            citizen = citizen,
            artisans = reorderedList,
            nearestArtisans = currentNearestArtisans,
            selectedArtisan = artisan
        )
    }

    fun loadArtisans(category: String? = null) {
        loadHome(category)
    }

    fun loadMyRequests() = viewModelScope.launch {
        _state.value = CitizenUiState.Loading
        citizenRepository.getMyRequests(currentUserId).fold(
            onSuccess = { _state.value = CitizenUiState.RequestsLoaded(it) },
            onFailure = { _state.value = CitizenUiState.Error(it.message ?: "Erreur") }
        )
    }

    fun loadOffers(requestId: String) = viewModelScope.launch {
        _state.value = CitizenUiState.Loading
        citizenRepository.getOffersForRequest(requestId).fold(
            onSuccess = { _state.value = CitizenUiState.OffersLoaded(it) },
            onFailure = { _state.value = CitizenUiState.Error(it.message ?: "Erreur") }
        )
    }

    fun acceptOffer(offer: Offer, request: Request) = viewModelScope.launch {
        _state.value = CitizenUiState.Loading
        citizenRepository.acceptOffer(offer, request).fold(
            onSuccess = { _state.value = CitizenUiState.OfferAccepted },
            onFailure = { _state.value = CitizenUiState.Error(it.message ?: "Erreur") }
        )
    }

    fun rejectOffer(offerId: String) = viewModelScope.launch {
        citizenRepository.rejectOffer(offerId).fold(
            onSuccess = { _state.value = CitizenUiState.OfferRejected },
            onFailure = { _state.value = CitizenUiState.Error(it.message ?: "Erreur") }
        )
    }

    fun loadArtisanProfile(artisanId: String) = viewModelScope.launch {
        _state.value = CitizenUiState.Loading
        citizenRepository.getArtisanById(artisanId).fold(
            onSuccess = { _state.value = CitizenUiState.ArtisanLoaded(it) },
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
            citizenId = currentUserId,
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

    // ★ NOUVEAU — charger les avis d'un artisan
    fun loadReviews(artisanId: String) = viewModelScope.launch {
        _state.value = CitizenUiState.Loading
        reviewRepository.getReviewsForArtisan(artisanId).fold(
            onSuccess = { _state.value = CitizenUiState.ReviewsLoaded(it) },
            onFailure = { _state.value = CitizenUiState.Error(it.message ?: "Erreur") }
        )
    }

    // ★ NOUVEAU — soumettre un avis
    fun submitReview(artisanId: String, artisanName: String, rating: Float, comment: String) =
        viewModelScope.launch {
            _state.value = CitizenUiState.Loading

            val alreadyReviewed = reviewRepository.hasAlreadyReviewed(artisanId, currentUserId)
            if (alreadyReviewed) {
                _state.value = CitizenUiState.AlreadyReviewed(artisanId)
                return@launch
            }

            val citizenName = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()
                .getString("name") ?: "Citoyen"

            val review = com.example.maalem.data.model.Review(
                artisanId = artisanId,
                citizenId = currentUserId,
                citizenName = citizenName,
                rating = rating,
                comment = comment
            )

            reviewRepository.addReview(review).fold(
                onSuccess = { _state.value = CitizenUiState.ReviewSubmitted },
                onFailure = { _state.value = CitizenUiState.Error(it.message ?: "Erreur") }
            )
        }
}