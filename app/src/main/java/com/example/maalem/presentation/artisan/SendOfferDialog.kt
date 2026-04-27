package com.example.maalem.presentation.artisan

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.maalem.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class SendOfferDialog(
    private val requestId: String,
    private val onSubmit: (price: Double, delay: String, message: String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_send_offer, null)
        val etPrice = view.findViewById<TextInputEditText>(R.id.etOfferPrice)
        val etDelay = view.findViewById<TextInputEditText>(R.id.etOfferDelay)
        val etMessage = view.findViewById<TextInputEditText>(R.id.etOfferMessage)

        return MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setPositiveButton("Envoyer") { _, _ ->
                val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
                val delay = etDelay.text.toString().trim()
                val message = etMessage.text.toString().trim()
                onSubmit(price, delay, message)
            }
            .setNegativeButton("Annuler", null)
            .create()
    }
}