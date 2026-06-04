package com.example.maalem.presentation.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.maalem.R
import com.example.maalem.databinding.ActivityAdminHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AdminDashboardFragment())
                .commit()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_stats -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AdminDashboardFragment())
                        .commit()
                    true
                }
                R.id.nav_artisans -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PendingArtisansFragment())
                        .commit()
                    true
                }
                R.id.nav_users -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ManageAccountsFragment())
                        .commit()
                    true
                }
                R.id.nav_categories -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CategoriesFragment())
                        .commit()
                    true
                }
                R.id.nav_notif -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SendNotificationFragment())
                        .commit()
                    true
                }

                else -> false

            }
        }
    }
}