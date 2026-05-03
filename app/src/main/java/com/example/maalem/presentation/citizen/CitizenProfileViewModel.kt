package com.example.maalem.presentation.citizen

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maalem.data.model.Citizen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import android.util.Base64

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Loaded(val citizen: Citizen) : ProfileState()
    object Updated : ProfileState()
    object LoggedOut : ProfileState()
    data class Error(val message: String) : ProfileState()
}

@HiltViewModel
class CitizenProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val state: StateFlow<ProfileState> = _state

    init { loadProfile() }

    fun loadProfile() = viewModelScope.launch {
        _state.value = ProfileState.Loading
        try {
            val uid = auth.currentUser?.uid ?: return@launch
            val doc = firestore.collection("users").document(uid).get().await()
            val citizen = Citizen(
                uid = uid,
                name = doc.getString("name") ?: "",
                email = doc.getString("email") ?: "",
                phone = doc.getString("phone") ?: "",
                address = doc.getString("address") ?: "",
                photoUrl = doc.getString("photoUrl") ?: ""
            )
            _state.value = ProfileState.Loaded(citizen)
        } catch (e: Exception) {
            _state.value = ProfileState.Error(e.message ?: "Erreur")
        }
    }

    fun updateProfile(name: String, phone: String, address: String) = viewModelScope.launch {
        _state.value = ProfileState.Loading
        try {
            val uid = auth.currentUser?.uid ?: return@launch
            if (name.isBlank()) {
                _state.value = ProfileState.Error("Le nom est requis")
                return@launch
            }
            firestore.collection("users").document(uid).update(
                mapOf(
                    "name" to name,
                    "phone" to phone,
                    "address" to address
                )
            ).await()
            _state.value = ProfileState.Updated
        } catch (e: Exception) {
            _state.value = ProfileState.Error(e.message ?: "Erreur")
        }
    }

    fun uploadPhoto(uri: Uri, context: Context) = viewModelScope.launch {
        _state.value = ProfileState.Loading
        try {
            val uid = auth.currentUser?.uid ?: return@launch

            // Convertir image en Base64
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            val resized = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
            val baos = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

            // Sauvegarder dans Firestore
            firestore.collection("users").document(uid)
                .update("photoUrl", "data:image/jpeg;base64,$base64").await()

            _state.value = ProfileState.Updated
        } catch (e: Exception) {
            _state.value = ProfileState.Error(e.message ?: "Erreur")
        }
    }
    fun logout() {
        auth.signOut()
        _state.value = ProfileState.LoggedOut
    }
}