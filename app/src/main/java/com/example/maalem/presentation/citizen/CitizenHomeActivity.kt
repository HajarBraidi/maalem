package com.example.maalem.presentation.citizen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.maalem.R
import com.example.maalem.databinding.ActivityCitizenHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CitizenHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCitizenHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCitizenHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fragment par défaut
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CitizenHomeFragment())
                .commit()
        }

        // Bottom Navigation
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CitizenHomeFragment())
                        .commit()
                    true
                }
                R.id.nav_request -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CreateRequestFragment())
                        .commit()
                    true
                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CitizenProfileFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}