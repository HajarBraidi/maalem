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
 * @param specialty  nom de la spécialité de l'application (ex: "Maconnerie")
 * @param confidence probabilité de la classe prédite (0f..1f)
 */
data class DefectPrediction(
    val specialty: String,
    val confidence: Float
)

/**
 * Classifieur de défauts de mur basé sur le modèle MobileNetV2 entraîné dans Maalem.
 *
 * Détails du modèle (vérifiés sur le .tflite) :
 *  - Entrée  : 1 x 224 x 224 x 3, float32, PIXELS BRUTS 0..255
 *              (le preprocess MobileNetV2 est intégré au modèle → NE PAS normaliser).
 *  - Sortie  : 1 x 3 probabilités softmax, ordre EXACT : [fissure, peinture, humidite].
 */
class WallDefectClassifier(context: Context) {

    private val interpreter: Interpreter

    companion object {
        private const val MODEL_FILE = "maalem_model.tflite"
        private const val IMG_SIZE = 224
        private const val NUM_CLASSES = 3

        // Ordre EXACT des sorties du modèle (ne pas changer)
        private val MODEL_CLASSES = listOf("fissure", "peinture", "humidite")

        // Correspondance classe du modèle → spécialité de l'application
        private val MODEL_TO_SPECIALTY = mapOf(
            "fissure" to "Maconnerie",
            "peinture" to "Peinture",
            "humidite" to "Etancheite"
        )

        // Seuil de confiance recommandé (cf. analyse du seuil dans le notebook).
        const val CONFIDENCE_THRESHOLD = 0.80f
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

        // Recopie systématique pour éviter un cache obsolète
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
     * Lance l'inférence sur une image et renvoie la prédiction
     * (spécialité de l'app + confiance).
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
        val specialty = MODEL_TO_SPECIALTY[modelClass] ?: modelClass

        return DefectPrediction(
            specialty = specialty,
            confidence = probs[bestIdx]
        )
    }

    /** À appeler quand on n'a plus besoin du classifieur. */
    fun close() {
        interpreter.close()
    }
}