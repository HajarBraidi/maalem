package com.example.maalem.presentation.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.maalem.R
import com.example.maalem.presentation.admin.AdminHomeActivity
import com.example.maalem.presentation.artisan.ArtisanHomeActivity
import com.example.maalem.presentation.citizen.CitizenHomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        if (auth.currentUser != null) {
            navigateToHome()
            return
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    private fun navigateToHome() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "citizen"

                //  Vérifier isActive au redémarrage aussi
                val isActive = doc.getBoolean("isActive") ?: true
                if (!isActive) {
                    auth.signOut()
                    // Rester sur LoginFragment avec message
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, LoginFragment().apply {
                            arguments = Bundle().apply {
                                putString("error", "Votre compte a été désactivé.")
                            }
                        })
                        .commit()
                    return@addOnSuccessListener
                }

                //  Vérifier isValidated pour les artisans
                val isValidated = if (role == "artisan") {
                    doc.getBoolean("isValidated") ?: false
                } else true

                val intent = when (role) {
                    "citizen" -> Intent(this, CitizenHomeActivity::class.java)
                    "artisan" -> Intent(this, ArtisanHomeActivity::class.java)
                    "admin"   -> Intent(this, AdminHomeActivity::class.java)
                    else      -> Intent(this, CitizenHomeActivity::class.java)
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                // En cas d'erreur → rester sur LoginFragment
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, LoginFragment())
                    .commit()
            }
    }
}