package com.example.maalem.presentation.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.maalem.R
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
                val intent = when (role) {
                    "citizen" -> Intent(this, CitizenHomeActivity::class.java)
                    // "artisan" → ArtisanHomeActivity (à développer plus tard)
                    // "admin"   → AdminHomeActivity   (à développer plus tard)
                    else -> Intent(this, CitizenHomeActivity::class.java)
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