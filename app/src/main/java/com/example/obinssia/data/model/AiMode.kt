package com.example.obinssia.data.model

import androidx.compose.ui.graphics.Color

enum class AiMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val accentColorHex: Long = 0xFFFF334B
) {
    RAPIDE(
        id = "rapide",
        title = "⚡ RAPIDE",
        subtitle = "Pour les questions simples et instantanées",
        icon = "⚡",
        accentColorHex = 0xFFFF4D4D
    ),
    EXPERT(
        id = "expert",
        title = "🧠 EXPERT",
        subtitle = "Pour les problèmes complexes et professionnels",
        icon = "🧠",
        accentColorHex = 0xFF9D4EDD
    ),
    ETUDE(
        id = "etude",
        title = "📚 ÉTUDE",
        subtitle = "Pour apprendre et comprendre progressivement",
        icon = "📚",
        accentColorHex = 0xFF3A86FF
    ),
    CODE(
        id = "code",
        title = "💻 CODE",
        subtitle = "Pour programmer, corriger et optimiser",
        icon = "💻",
        accentColorHex = 0xFF06D6A0
    ),
    CREATEUR(
        id = "createur",
        title = "✍️ CRÉATEUR",
        subtitle = "Pour scripts, histoires, publicités et viralité",
        icon = "✍️",
        accentColorHex = 0xFFFFB703
    ),
    BUSINESS(
        id = "business",
        title = "💼 BUSINESS",
        subtitle = "Pour stratégie, marketing, vente et SWOT",
        icon = "💼",
        accentColorHex = 0xFFE50914
    );

    companion object {
        fun fromId(id: String): AiMode {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: RAPIDE
        }
    }
}
