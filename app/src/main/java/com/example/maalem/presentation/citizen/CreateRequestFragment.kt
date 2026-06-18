package com.example.maalem.presentation.citizen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.maalem.R
import com.example.maalem.data.model.AppLocation
import com.example.maalem.databinding.FragmentCreateRequestBinding
import com.example.maalem.domain.repository.CategoryRepository
import com.example.maalem.domain.repository.LocationRepository
import com.example.maalem.ml.WallDefectClassifier
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class CreateRequestFragment : Fragment(R.layout.fragment_create_request) {

    private var _binding: FragmentCreateRequestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CitizenHomeViewModel by viewModels()

    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var firestore: FirebaseFirestore
    @Inject lateinit var categoryRepository: CategoryRepository
    @Inject lateinit var locationRepository: LocationRepository

    private var categories: List<String> = emptyList()
    private var cities: List<AppLocation> = emptyList()

    // Photo choisie, encodée en Base64 (vide si aucune)
    private var photoBase64: String = ""

    // Classifieur IA (chargé à la demande, libéré dans onDestroyView)
    private var classifier: WallDefectClassifier? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onPhotoPicked(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateRequestBinding.bind(view)

        loadCategories()
        loadCities()

        binding.photoContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSubmit.setOnClickListener {
            submitRequest()
        }

        observeState()
    }

    private fun onPhotoPicked(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Redimensionnement à 224x224 (entrée du modèle + stockage léger)
            val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

            // Aperçu
            binding.ivPhotoPreview.setImageBitmap(resized)
            binding.ivPhotoPreview.isVisible = true
            binding.photoPlaceholder.isVisible = false

            // Encodage Base64 pour Firestore
            val baos = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            photoBase64 = "data:image/jpeg;base64," +
                    Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

            // Lancer la prédiction IA
            classifyPhoto(resized)

        } catch (e: Exception) {
            Snackbar.make(
                binding.root,
                "Impossible de charger l'image",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Analyse la photo avec le modèle TFLite et pré-remplit le titre, la description
     * et la catégorie si la prédiction est fiable et dans le domaine.
     */
    private fun classifyPhoto(bitmap: Bitmap) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val prediction = withContext(Dispatchers.Default) {
                    val clf = classifier ?: WallDefectClassifier(requireContext().applicationContext)
                        .also { classifier = it }
                    clf.classify(bitmap)
                }

                val percent = (prediction.confidence * 100).toInt()

                when {
                    // Cas 1 : photo hors-contexte (classe "autre")
                    prediction.isOutOfScope -> {
                        Snackbar.make(
                            binding.root,
                            "Image non reconnue comme un défaut de mur. Choisissez la catégorie manuellement.",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    // Cas 2 : confiance suffisante + catégorie présente dans la liste
                    prediction.confidence >= WallDefectClassifier.CONFIDENCE_THRESHOLD
                            && prediction.specialty != null
                            && categories.any { it.equals(prediction.specialty, ignoreCase = true) } -> {
                        // Pré-remplir la catégorie
                        binding.etCategory.setText(prediction.specialty, false)

                        // Pré-remplir le titre s'il est vide
                        if (binding.etTitle.text.isNullOrBlank()
                            && prediction.suggestedTitle.isNotEmpty()) {
                            binding.etTitle.setText(prediction.suggestedTitle)
                        }

                        // Pré-remplir la description si elle est vide
                        if (binding.etDescription.text.isNullOrBlank()
                            && prediction.suggestedDescription.isNotEmpty()) {
                            binding.etDescription.setText(prediction.suggestedDescription)
                        }

                        Snackbar.make(
                            binding.root,
                            "Catégorie détectée : ${prediction.specialty} ($percent%)",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    // Cas 3 : catégorie prédite absente de la liste Firestore
                    prediction.specialty != null
                            && categories.none { it.equals(prediction.specialty, ignoreCase = true) } -> {
                        Snackbar.make(
                            binding.root,
                            "Catégorie détectée (${prediction.specialty}) absente de la liste. Choisissez manuellement.",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    // Cas 4 : confiance trop faible
                    else -> {
                        Snackbar.make(
                            binding.root,
                            "Analyse peu fiable ($percent%). Choisissez la catégorie manuellement.",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Snackbar.make(
                    binding.root,
                    "Analyse de l'image indisponible. Choisissez la catégorie manuellement.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadCities() {
        viewLifecycleOwner.lifecycleScope.launch {
            locationRepository.getCities().onSuccess { list ->
                cities = list
                binding.etCity.setAdapter(
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        list.map { it.name }
                    )
                )
            }.onFailure {
                Snackbar.make(
                    binding.root,
                    "Erreur lors du chargement des villes",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = categoryRepository.getCategories()

            result.onSuccess { list ->
                categories = list

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    categories
                )

                binding.etCategory.setAdapter(adapter)
            }

            result.onFailure {
                Snackbar.make(
                    binding.root,
                    "Erreur lors du chargement des catégories",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is CitizenUiState.Loading
                binding.btnSubmit.isEnabled = state !is CitizenUiState.Loading

                when (state) {
                    is CitizenUiState.RequestSent -> {
                        Snackbar.make(
                            binding.root,
                            "Demande envoyée avec succès !",
                            Snackbar.LENGTH_LONG
                        ).show()
                        clearForm()
                    }

                    is CitizenUiState.Error -> {
                        Snackbar.make(
                            binding.root,
                            state.message,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun submitRequest() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val category = binding.etCategory.text.toString().trim()
        val city = binding.etCity.text.toString().trim()

        if (title.isEmpty()) {
            Snackbar.make(binding.root, "Veuillez entrer un titre", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Snackbar.make(binding.root, "Veuillez entrer une description", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (category.isEmpty()) {
            Snackbar.make(binding.root, "Veuillez choisir une catégorie", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (!categories.contains(category)) {
            Snackbar.make(binding.root, "Veuillez choisir une catégorie valide", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (city.isEmpty()) {
            Snackbar.make(binding.root, "Veuillez choisir une ville", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (cities.none { it.name.equals(city, ignoreCase = true) }) {
            Snackbar.make(binding.root, "Veuillez choisir une ville valide", Snackbar.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: ""

                viewModel.createRequest(
                    title = title,
                    description = description,
                    category = category,
                    city = city,
                    citizenName = name,
                    photoBase64 = photoBase64
                )
            }
            .addOnFailureListener {
                Snackbar.make(
                    binding.root,
                    "Impossible de récupérer les informations utilisateur",
                    Snackbar.LENGTH_LONG
                ).show()
            }
    }

    private fun clearForm() {
        binding.etTitle.text?.clear()
        binding.etDescription.text?.clear()
        binding.etCategory.text?.clear()
        binding.etCity.text?.clear()

        // Réinitialiser la photo
        photoBase64 = ""
        binding.ivPhotoPreview.setImageDrawable(null)
        binding.ivPhotoPreview.isVisible = false
        binding.photoPlaceholder.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        classifier?.close()
        classifier = null
        _binding = null
    }
}