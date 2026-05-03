package com.example.maalem.presentation.artisan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.maalem.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtisanHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.artisanBottomNav)

        // Charger le premier fragment par défaut
        loadFragment(ArtisanRequestsFragment())

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_artisan_requests -> ArtisanRequestsFragment()
                R.id.nav_artisan_offers -> ArtisanOffersFragment()
                R.id.nav_artisan_profile -> ArtisanProfileFragment()
                else -> ArtisanRequestsFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.artisanFragmentContainer, fragment)
            .commit()
    }
}