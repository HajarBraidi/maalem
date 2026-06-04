package com.example.maalem.presentation.citizen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.maalem.R
import com.example.maalem.databinding.ActivityCitizenHomeBinding
import com.example.maalem.presentation.auth.AuthActivity
import com.example.maalem.presentation.chat.ChatViewModel
import com.example.maalem.presentation.chat.ConversationsFragment
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CitizenHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCitizenHomeBinding

    @Inject
    lateinit var auth: FirebaseAuth

    private val chatViewModel: ChatViewModel by viewModels()

    private var unreadJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCitizenHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        observeUnreadMessages()
        setupBottomNav(savedInstanceState)

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        //  ORDRE CRITIQUE : arrêter Firestore AVANT signOut
        unreadJob?.cancel()
        unreadJob = null
        chatViewModel.stopListening()  // ← coupe le Flow Firestore

        auth.signOut()

        startActivity(Intent(this, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun observeUnreadMessages() {
        unreadJob = lifecycleScope.launch {
            chatViewModel.unreadCount.collect { count ->
                try {
                    val badge = binding.bottomNav.getOrCreateBadge(R.id.nav_chat)
                    if (count > 0) {
                        badge.isVisible = true
                        badge.number = count
                    } else {
                        badge.isVisible = false
                    }
                } catch (e: Exception) {
                    Log.e("CitizenHomeActivity", "Badge error", e)
                }
            }
        }
    }

    private fun setupBottomNav(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CitizenHomeFragment())
                .commit()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> CitizenHomeFragment()
                R.id.nav_my_requests -> MyRequestsFragment()
                R.id.nav_request -> CreateRequestFragment()
                R.id.nav_profile -> CitizenProfileFragment()
                R.id.nav_chat -> ConversationsFragment()
                else -> return@setOnItemSelectedListener false
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            true
        }
    }

    override fun onDestroy() {
        unreadJob?.cancel()
        unreadJob = null
        super.onDestroy()
    }
}