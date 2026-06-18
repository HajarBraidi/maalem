package com.example.maalem.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Résultat d'une prédiction.
 *
 * @param specialty            spécialité de l'application (ex: "Maconnerie"), ou null si
 *                             l'image est classée "autre" (hors-domaine) → saisie manuelle.
 * @param confidence           probabilité de la classe prédite (0f..1f).
 * @param isOutOfScope         true si la classe prédite est "autre" (photo hors-contexte).
 * @param suggestedTitle       titre proposé pour la catégorie détectée (vide si hors-domaine).
 * @param suggestedDescription description proposée pour la catégorie détectée (vide si hors-domaine).
 */
data class DefectPrediction(
    val specialty: String?,
    val confidence: Float,
    val isOutOfScope: Boolean,
    val suggestedTitle: String = "",
    val suggestedDescription: String = ""
)

/**
 * Classifieur de défauts de mur basé sur le modèle MobileNetV2 entraîné dans Maalem.
 *
 * Détails du modèle (vérifiés sur le .tflite) :
 *  - Entrée  : 1 x 224 x 224 x 3, float32, PIXELS BRUTS 0..255
 *              (le preprocess MobileNetV2 est intégré au modèle → NE PAS normaliser).
 *  - Sortie  : 1 x 4 probabilités softmax, ordre EXACT (cf. labels.txt) :
 *              [fissure, peinture, humidite, autre].
 *
 * La classe "autre" (murs sains + images hors-domaine) sert à rejeter les photos
 * qui ne sont pas des défauts de mur : aucune spécialité n'est proposée et
 * l'utilisateur choisit manuellement.
 */
class WallDefectClassifier(context: Context) {

    private val interpreter: Interpreter

    companion object {
        private const val MODEL_FILE = "maalem_model.tflite"
        private const val IMG_SIZE = 224
        private const val NUM_CLASSES = 4

        // Ordre EXACT des sorties du modèle (= labels.txt). Ne pas changer.
        private val MODEL_CLASSES = listOf("fissure", "peinture", "humidite", "autre")

        // Correspondance classe du modèle → spécialité de l'application.
        // "autre" est volontairement absent : pas de spécialité → saisie manuelle.
        private val MODEL_TO_SPECIALTY = mapOf(
            "fissure" to "Maconnerie",
            "peinture" to "Peinture",
            "humidite" to "Etancheite"
        )

        // Titre suggéré selon la catégorie détectée (le citoyen peut le modifier).
        private val MODEL_TO_TITLE = mapOf(
            "fissure"  to "Réparation de fissure",
            "peinture" to "Reprise de peinture",
            "humidite" to "Traitement d'humidité"
        )

        // Description suggérée selon la catégorie détectée (le citoyen peut la modifier).
        private val MODEL_TO_DESCRIPTION = mapOf(
            "fissure"  to "Présence d'une fissure sur le mur à réparer.",
            "peinture" to "Peinture écaillée ou abîmée à reprendre.",
            "humidite" to "Traces d'humidité ou infiltration d'eau à traiter."
        )

        // Seuil de confiance (cf. cellule 9.4 du notebook : couverture vs précision).
        // En dessous, on ne pré-remplit pas → l'utilisateur choisit manuellement.
        const val CONFIDENCE_THRESHOLD = 0.70f
    }

    init {
        val modelFile = copyAssetToCache(context, MODEL_FILE)
        val options = Interpreter.Options().apply { numThreads = 2 }
        interpreter = Interpreter(modelFile, options)
    }

    /**
     * Copie le modèle depuis les assets vers le cache de l'app, puis renvoie le File.
     * Insensible à la compression des assets (contrairement au memory-mapping direct).
     */
    private fun copyAssetToCache(context: Context, assetName: String): File {
        val outFile = File(context.cacheDir, assetName)
        context.assets.open(assetName).use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
        return outFile
    }

    /**
     * Convertit un Bitmap en ByteBuffer float32 de pixels bruts 0..255.
     * AUCUNE normalisation : MobileNetV2 s'en charge en interne.
     */
    private fun bitmapToBuffer(bitmap: Bitmap): ByteBuffer {
        val resized = if (bitmap.width != IMG_SIZE || bitmap.height != IMG_SIZE) {
            Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true)
        } else {
            bitmap
        }

        val buffer = ByteBuffer.allocateDirect(4 * IMG_SIZE * IMG_SIZE * 3)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(IMG_SIZE * IMG_SIZE)
        resized.getPixels(pixels, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE)

        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF).toFloat()
            val g = (pixel shr 8 and 0xFF).toFloat()
            val b = (pixel and 0xFF).toFloat()
            buffer.putFloat(r)
            buffer.putFloat(g)
            buffer.putFloat(b)
        }

        buffer.rewind()
        return buffer
    }

    /**
     * Lance l'inférence sur une image et renvoie la prédiction.
     */
    fun classify(bitmap: Bitmap): DefectPrediction {
        val input = bitmapToBuffer(bitmap)
        val output = Array(1) { FloatArray(NUM_CLASSES) }

        interpreter.run(input, output)

        val probs = output[0]
        var bestIdx = 0
        for (i in 1 until NUM_CLASSES) {
            if (probs[i] > probs[bestIdx]) bestIdx = i
        }

        val modelClass = MODEL_CLASSES[bestIdx]
        val isOutOfScope = (modelClass == "autre")
        val specialty = if (isOutOfScope) null else MODEL_TO_SPECIALTY[modelClass]
        val suggestedTitle = if (isOutOfScope) "" else (MODEL_TO_TITLE[modelClass] ?: "")
        val suggestedDescription = if (isOutOfScope) "" else (MODEL_TO_DESCRIPTION[modelClass] ?: "")

        return DefectPrediction(
            specialty = specialty,
            confidence = probs[bestIdx],
            isOutOfScope = isOutOfScope,
            suggestedTitle = suggestedTitle,
            suggestedDescription = suggestedDescription
        )
    }

    /** À appeler quand on n'a plus besoin du classifieur. */
    fun close() {
        interpreter.close()
    }
}