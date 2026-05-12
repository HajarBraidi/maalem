package com.example.maalem.presentation.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SendNotificationFragment : Fragment() {

    private val viewModel: AdminViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(requireContext()).apply {
            text = " Envoyer une notification"
            textSize = 20f
            setPadding(0, 0, 0, 32)
        }

        val etTitle = TextInputEditText(requireContext()).apply {
            hint = "Titre"
        }
        val layoutTitle = TextInputLayout(requireContext()).apply {
            addView(etTitle)
            hint = "Titre de la notification"
        }

        val etMessage = TextInputEditText(requireContext()).apply {
            hint = "Message"
            minLines = 3
        }
        val layoutMessage = TextInputLayout(requireContext()).apply {
            addView(etMessage)
            hint = "Message"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 24 }
        }

        val progressBar = ProgressBar(requireContext()).apply {
            isVisible = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val btnSend = Button(requireContext()).apply {
            text = " Envoyer"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 32 }
        }

        layout.addView(title)
        layout.addView(layoutTitle)
        layout.addView(layoutMessage)
        layout.addView(progressBar)
        layout.addView(btnSend)

        btnSend.setOnClickListener {
            viewModel.sendNotification(
                title = etTitle.text.toString(),
                message = etMessage.text.toString()
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                progressBar.isVisible = state is AdminUiState.Loading
                when (state) {
                    is AdminUiState.ActionSuccess -> {
                        Snackbar.make(layout, "✅ Notification envoyée !", Snackbar.LENGTH_SHORT).show()
                        etTitle.text?.clear()
                        etMessage.text?.clear()
                    }
                    is AdminUiState.Error -> {
                        Snackbar.make(layout, state.message, Snackbar.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }

        return layout
    }
}