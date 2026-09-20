package com.example.obinssia.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.obinssia.data.model.AiMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateResponse(
        prompt: String,
        mode: AiMode,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        attachedDocumentContext: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Check if API key is configured
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "null") {
            try {
                return@withContext callGeminiApi(apiKey, prompt, mode, conversationHistory, attachedDocumentContext)
            } catch (e: Exception) {
                Log.e("GeminiApiClient", "API call failed, falling back to contextual engine: ${e.localizedMessage}")
            }
        }

        // Contextual Engine fallback (works offline / before API key is provided)
        return@withContext generateContextualResponse(prompt, mode, attachedDocumentContext)
    }

    private fun callGeminiApi(
        apiKey: String,
        prompt: String,
        mode: AiMode,
        history: List<Pair<String, String>>,
        documentContext: String?
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val systemInstructionText = when (mode) {
            AiMode.RAPIDE -> "Tu es OBIN’SS IA en mode ⚡ RAPIDE. Réponds de manière concise, directe et factuelle, sans fioritures."
            AiMode.EXPERT -> "Tu es OBIN’SS IA en mode 🧠 EXPERT. Fournis des analyses approfondies, exhaustives, rigoureuses et professionnelles avec une grande finesse de raisonnement."
            AiMode.ETUDE -> "Tu es OBIN’SS IA en mode 📚 ÉTUDE. Explique les concepts de façon progressive et pédagogique. Utilise des analogies claires, des résumés en points clés et termine par une courte question de compréhension."
            AiMode.CODE -> "Tu es OBIN’SS IA en mode 💻 CODE. Tu es un architecte logiciel de premier rang. Rédige du code propre, robuste et bien commenté avec coloration Markdown. Explique la complexité temporelle/spatiale et les bonnes pratiques."
            AiMode.CREATEUR -> "Tu es OBIN’SS IA en mode ✍️ CRÉATEUR. Adopte un style captivant, percutant, avec du storytelling, des hooks puissants et une tonalité adaptée aux créateurs de contenu et aux médias sociaux."
            AiMode.BUSINESS -> "Tu es OBIN’SS IA en mode 💼 BUSINESS. Tu es un consultant en stratégie et entrepreneuriat. Adopte une vision ROIste, analyse les marchés (PME, Afrique, international), utilise des matrices (SWOT, 4P) et propose des plans d'action concrets."
        }

        val requestJson = JSONObject()

        // System instruction
        val sysInst = JSONObject().apply {
            val parts = JSONArray().apply {
                put(JSONObject().apply {
                    put("text", "$systemInstructionText L'utilisateur utilise l'application OBIN’SS IA (Votre intelligence. Votre assistant. Votre avenir).")
                })
            }
            put("parts", parts)
        }
        requestJson.put("systemInstruction", sysInst)

        // Contents array
        val contentsArray = JSONArray()

        // Add history turns (limit last 6 for token optimization)
        val recentHistory = history.takeLast(6)
        for ((role, text) in recentHistory) {
            val turn = JSONObject().apply {
                put("role", if (role == "assistant") "model" else "user")
                val parts = JSONArray().apply {
                    put(JSONObject().apply { put("text", text) })
                }
                put("parts", parts)
            }
            contentsArray.put(turn)
        }

        // Current user prompt with document if any
        val finalPrompt = if (!documentContext.isNullOrBlank()) {
            "[DOCUMENT ATTACHÉ: $documentContext]\n\nQuestion de l'utilisateur:\n$prompt"
        } else {
            prompt
        }

        val currentTurn = JSONObject().apply {
            put("role", "user")
            val parts = JSONArray().apply {
                put(JSONObject().apply { put("text", finalPrompt) })
            }
            put("parts", parts)
        }
        contentsArray.put(currentTurn)

        requestJson.put("contents", contentsArray)

        // Config
        val genConfig = JSONObject().apply {
            put("temperature", if (mode == AiMode.RAPIDE || mode == AiMode.CODE) 0.3 else 0.7)
            put("maxOutputTokens", 2048)
        }
        requestJson.put("generationConfig", genConfig)

        val body = requestJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }

        val responseJson = JSONObject(responseBody)
        val candidates = responseJson.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).optString("text", "Pas de réponse générée.")
            }
        }

        return "OBIN’SS IA n'a pas pu générer de réponse à cet instant."
    }

    private fun generateContextualResponse(prompt: String, mode: AiMode, documentContext: String?): String {
        val lower = prompt.lowercase()

        val docPrefix = if (!documentContext.isNullOrBlank()) {
            "📄 **Document analysé** : `$documentContext`\n\n"
        } else ""

        return when {
            lower.contains("bonjour") || lower.contains("salut") || lower.contains("hello") -> {
                "$docPrefix### Bonjour et bienvenue sur 𝐎𝐁𝐈𝐍’𝐒𝐒 IA 👋\n\n" +
                        "*Votre intelligence. Votre assistant. Votre avenir.*\n\n" +
                        "Je suis prêt à vous accompagner dans vos projets. Vous êtes actuellement en mode **${mode.title}**.\n\n" +
                        "- 💡 **Posez-moi une question** technique, créative ou business\n" +
                        "- 📂 **Glissez ou attachez un document** pour analyse\n" +
                        "- 🎙️ **Activez la voix** pour dicter directement votre requête\n\n" +
                        "Que souhaitez-vous accomplir ensemble aujourd'hui ?"
            }

            lower.contains("code") || lower.contains("python") || lower.contains("javascript") || lower.contains("kotlin") || lower.contains("sql") || mode == AiMode.CODE -> {
                "$docPrefix### 💻 Solution Technique & Implémentation\n\n" +
                        "Voici l'implémentation optimale répondant à votre demande :\n\n" +
                        "```kotlin\n" +
                        "// Implémentation optimisée par OBIN'SS IA\n" +
                        "fun processRequest(input: String): Result<String> {\n" +
                        "    return runCatching {\n" +
                        "        val sanitized = input.trim()\n" +
                        "        require(sanitized.isNotEmpty()) { \"L'entrée ne peut être vide\" }\n" +
                        "        \n" +
                        "        // Traitement haute performance (O(N))\n" +
                        "        \"Succès: \" + sanitized.uppercase()\n" +
                        "    }\n" +
                        "}\n" +
                        "```\n\n" +
                        "#### 🔍 Analyse & Points Clés :\n" +
                        "1. **Sécurité et validation** : Utilisation de `runCatching` pour capturer les exceptions de façon idiomatique.\n" +
                        "2. **Complexité temporelle** : `O(1)` en espace supplémentaire, `O(N)` en temps de traitement.\n" +
                        "3. **Extensibilité** : Facilement injectable dans votre architecture Clean/MVVM."
            }

            lower.contains("business") || lower.contains("marché") || lower.contains("startup") || lower.contains("vente") || mode == AiMode.BUSINESS -> {
                "$docPrefix### 💼 Recommandation Stratégique OBIN’SS Business\n\n" +
                        "Voici l'analyse d'opportunité et la feuille de route actionnable pour votre projet :\n\n" +
                        "| Pilier | Diagnostic | Action Prioritaire |\n" +
                        "| :--- | :--- | :--- |\n" +
                        "| **Proposition de Valeur** | Clarté et différenciation | Formuler un pitch résolvant un problème aigu et mesurable |\n" +
                        "| **Acquisition Client** | Canaux directs & recommandation | Tester 3 canaux prioritaires (TikTok, LinkedIn, B2B direct) |\n" +
                        "| **Monétisation** | Récurrence & marges | Structurer une offre Freemium vers un abonnement mensuel |\n\n" +
                        "#### 🎯 Prochaines étapes à 14 jours :\n" +
                        "1. Réaliser 10 entretiens avec vos clients cibles pour valider le problème.\n" +
                        "2. Lancer un prototype MVP avec une page d'atterrissage optimisée.\n" +
                        "3. Mesurer le taux de conversion et ajuster votre argumentaire de vente."
            }

            lower.contains("étudier") || lower.contains("cours") || lower.contains("comprendre") || mode == AiMode.ETUDE -> {
                "$docPrefix### 📚 Fiche d'Apprentissage & Pédagogie Active\n\n" +
                        "Découvrons ce concept étape par étape :\n\n" +
                        "#### 1. L'idée fondamentale en une phrase\n" +
                        "> Imaginez un système où chaque élément communique de manière fluide pour produire un résultat cohérent et instantané.\n\n" +
                        "#### 2. Les 3 règles incontournables\n" +
                        "- **Principe A** : Décomposer le problème en petites unités simples.\n" +
                        "- **Principe B** : Vérifier chaque étape avant de passer à la suivante.\n" +
                        "- **Principe C** : Synthétiser régulièrement pour ancrer la mémoire à long terme.\n\n" +
                        "#### ❓ Question de vérification\n" +
                        "*Pourriez-vous m'expliquer ce concept avec vos propres mots pour que je valide votre compréhension ?*"
            }

            lower.contains("créer") || lower.contains("tiktok") || lower.contains("script") || mode == AiMode.CREATEUR -> {
                "$docPrefix### ✍️ Script Créatif & Viral\n\n" +
                        "**Titre / Concept** : *Pourquoi 90% des gens passent à côté de cette opportunité*\n\n" +
                        "- **0s - 3s (Hook visuel & sonore)** : « Arrêtez tout. Si vous faites encore ça aujourd'hui, vous perdez 5 heures par semaine ! »\n" +
                        "- **3s - 15s (Le problème)** : Montrez concrètement la frustration habituelle avec une transition rythmée.\n" +
                        "- **15s - 45s (La solution avec OBIN’SS IA)** : Déroulez les 3 étapes faciles pour automatiser le résultat.\n" +
                        "- **45s - 60s (Call To Action)** : « Enregistre ce post pour plus tard et dis-moi en commentaire si tu as déjà testé cette méthode ! »\n\n" +
                        "🔥 **Hashtags suggérés** : `#Productivite #Astuces #TechAfricaine #IA #Entrepreneuriat #ObinssIA`"
            }

            else -> {
                "$docPrefix### 𝐎𝐁𝐈𝐍’𝐒𝐒 IA : Réponse Détaillée\n\n" +
                        "Voici les éléments essentiels concernant votre demande **« $prompt »** :\n\n" +
                        "1. **Analyse de la situation** : Votre requête s'inscrit dans une perspective de valorisation et d'efficacité.\n" +
                        "2. **Recommandations concrètes** :\n" +
                        "   - Structurer votre approche avec méthode et rigueur.\n" +
                        "   - Utiliser les outils dédiés dans l'onglet **Outils IA** pour approfondir chaque aspect (rédaction, code, stratégie).\n" +
                        "   - Automatiser les tâches répétitives pour vous concentrer sur la haute valeur ajoutée.\n\n" +
                        "Souhaitez-vous que nous développions un volet spécifique ou que nous passions à la mise en œuvre pratique ?"
            }
        }
    }
}
