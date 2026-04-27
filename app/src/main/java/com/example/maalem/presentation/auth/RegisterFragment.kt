package com.example.maalem.presentation.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.maalem.R
import com.example.maalem.data.model.Specialty
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val viewModel: RegisterViewModel by viewModels()

    // Launcher pour sélectionner une image
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                viewModel.setCinUri(uri)
                view?.findViewById<ImageView>(R.id.ivCinPreview)?.apply {
                    setImageURI(uri)
                    visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Références aux vues
        val rgRole = view.findViewById<RadioGroup>(R.id.rgRole)
        val citizenFields = view.findViewById<LinearLayout>(R.id.citizenFields)
        val artisanFields = view.findViewById<LinearLayout>(R.id.artisanFields)
        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPhone = view.findViewById<TextInputEditText>(R.id.etPhone)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val etAddress = view.findViewById<TextInputEditText>(R.id.etAddress)
        val spSpecialty = view.findViewById<Spinner>(R.id.spSpecialty)
        val etCity = view.findViewById<TextInputEditText>(R.id.etCity)
        val etBio = view.findViewById<TextInputEditText>(R.id.etBio)
        val btnPickCin = view.findViewById<Button>(R.id.btnPickCin)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = view.findViewById<TextView>(R.id.tvGoToLogin)
        val progress = view.findViewById<ProgressBar>(R.id.registerProgress)

        // Initialiser le Spinner des spécialités
        val specialtyAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Specialty.displayNames()
        )
        specialtyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSpecialty.adapter = specialtyAdapter

        // Switch entre citoyen et artisan
        rgRole.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCitizen -> {
                    citizenFields.visibility = View.VISIBLE
                    artisanFields.visibility = View.GONE
                }
                R.id.rbArtisan -> {
                    citizenFields.visibility = View.GONE
                    artisanFields.visibility = View.VISIBLE
                }
            }
        }

        // Picker photo CIN
        btnPickCin.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        // Aller au login
        tvGoToLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Bouton inscription
        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (name.isBlank() || email.isBlank() || phone.isBlank()) {
                Snackbar.make(view, "Tous les champs sont requis", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (rgRole.checkedRadioButtonId == R.id.rbCitizen) {
                // Inscription CITOYEN
                val address = etAddress.text.toString().trim()
                viewModel.registerCitizen(name, email, phone, password, confirmPassword, address)
            } else {
                // Inscription ARTISAN
                if (!viewModel.hasCinPhoto()) {
                    Snackbar.make(view, "Photo CIN obligatoire", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val specialtyIdx = spSpecialty.selectedItemPosition
                val specialtyValue = Specialty.entries[specialtyIdx].value
                val city = etCity.text.toString().trim()
                val bio = etBio.text.toString().trim()

                if (city.isBlank() || bio.isBlank()) {
                    Snackbar.make(view, "Ville et bio requises", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                viewModel.registerArtisan(
                    context = requireContext(),
                    name = name,
                    email = email,
                    phone = phone,
                    password = password,
                    confirmPassword = confirmPassword,
                    specialty = specialtyValue,
                    city = city,
                    bio = bio
                )
            }
        }

        // Observer le state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                progress.isVisible = state is RegisterState.Loading
                btnRegister.isEnabled = state !is RegisterState.Loading

                when (state) {
                    is RegisterState.Success -> {
                        val msg = if (rgRole.checkedRadioButtonId == R.id.rbArtisan) {
                            "✓ Inscription réussie ! Votre compte artisan sera validé par l'admin."
                        } else {
                            "✓ Inscription réussie ! Connectez-vous."
                        }
                        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show()
                        // Retour au login
                        view.postDelayed({
                            parentFragmentManager.popBackStack()
                        }, 1500)
                    }
                    is RegisterState.Error -> {
                        Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }
    }
}