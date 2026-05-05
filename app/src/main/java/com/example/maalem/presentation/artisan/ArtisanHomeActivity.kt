package com.example.maalem.presentation.artisan

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.maalem.R
import com.example.maalem.databinding.ActivityArtisanHomeBinding
import com.example.maalem.presentation.chat.ChatViewModel
import com.example.maalem.presentation.chat.ConversationsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArtisanHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtisanHomeBinding
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityArtisanHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(ArtisanRequestsFragment())
        }

        binding.artisanBottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_artisan_requests -> ArtisanRequestsFragment()
                R.id.nav_artisan_offers -> ArtisanOffersFragment()
                R.id.nav_artisan_profile -> ArtisanProfileFragment()
                R.id.nav_artisan_chat -> ConversationsFragment()
                else -> ArtisanRequestsFragment()
            }

            loadFragment(fragment)
            true
        }

        observeUnreadMessages()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.artisanFragmentContainer, fragment)
            .commit()
    }

    private fun observeUnreadMessages() {
        lifecycleScope.launch {
            chatViewModel.unreadCount.collect { count ->
                val badge = binding.artisanBottomNav.getOrCreateBadge(R.id.nav_artisan_chat)

                if (count > 0) {
                    badge.isVisible = true
                    badge.number = count
                } else {
                    badge.isVisible = false
                }
            }
        }
    }
}