package com.example.obinssia.data.model

data class AiToolField(
    val id: String,
    val label: String,
    val placeholder: String,
    val defaultValue: String = "",
    val isMultiLine: Boolean = false,
    val options: List<String> = emptyList()
)

data class AiTool(
    val id: String,
    val title: String,
    val category: ToolCategory,
    val icon: String,
    val description: String,
    val fields: List<AiToolField>,
    val promptTemplate: (Map<String, String>) -> String
)

enum class ToolCategory(val title: String, val icon: String, val description: String) {
    ECRITURE("✍️ Écriture", "✍️", "Rédaction, correction, reformulation, résumé et traduction"),
    CREATION("🎨 Création", "🎨", "Génération de prompts, publicité, scripts vidéo, storytelling"),
    BUSINESS("💼 Business", "💼", "Business plan, marketing, stratégie, étude de marché, SWOT"),
    RESEAUX_SOCIAUX("📱 Réseaux Sociaux", "📱", "TikTok, Facebook, Instagram, YouTube"),
    ETUDES("📚 Études", "📚", "Cours, fiches de révision, quiz, exercices et correction"),
    PROGRAMMATION("💻 Programmation", "💻", "Génération de code, correction de code, explication")
}

object AiToolsCatalog {
    val allTools: List<AiTool> = listOf(
        // ======================= ÉCRITURE =======================
        AiTool(
            id = "generateur_texte",
            title = "Rédaction de texte",
            category = ToolCategory.ECRITURE,
            icon = "📝",
            description = "Rédigez des articles, courriers ou contenus sur mesure.",
            fields = listOf(
                AiToolField("sujet", "Sujet ou thématique", "Ex: L'impact de la technologie en Afrique"),
                AiToolField("ton", "Ton souhaité", "Professionnel, Captivant, Neutre, Éloquent", defaultValue = "Professionnel"),
                AiToolField("longueur", "Longueur désirée", "Court, Moyen, Détaillé", defaultValue = "Moyen")
            ),
            promptTemplate = { params ->
                "Agis comme un rédacteur d'élite pour OBIN’SS IA. Rédige un texte complet sur le sujet suivant: ${params["sujet"]}.\nTon: ${params["ton"]}.\nLongueur: ${params["longueur"]}.\nStructure le texte avec des sous-titres clairs et une conclusion marquante."
            }
        ),
        AiTool(
            id = "correction",
            title = "Correction de texte",
            category = ToolCategory.ECRITURE,
            icon = "✨",
            description = "Corrigez l'orthographe, la grammaire et la ponctuation.",
            fields = listOf(
                AiToolField("texte", "Texte à corriger", "Collez votre texte ici...", isMultiLine = true)
            ),
            promptTemplate = { params ->
                "Corrige minutieusement l'orthographe, la grammaire et la syntaxe du texte suivant. Fournis la version parfaitement corrigée, puis liste les principales corrections apportées avec une brève justification :\n\n${params["texte"]}"
            }
        ),
        AiTool(
            id = "reformulation",
            title = "Reformulation de texte",
            category = ToolCategory.ECRITURE,
            icon = "🔄",
            description = "Réécrivez vos phrases avec un style plus fluide ou percutant.",
            fields = listOf(
                AiToolField("texte", "Texte à reformuler", "Collez votre texte...", isMultiLine = true),
                AiToolField("style", "Style visé", "Plus professionnel, Plus concis, Plus vendeur, Plus simple", defaultValue = "Plus professionnel")
            ),
            promptTemplate = { params ->
                "Reformule le texte ci-dessous dans le style suivant : ${params["style"]}.\n\nTexte source :\n${params["texte"]}\n\nDonne 2 variations de reformulation distinctes et prêtes à l'emploi."
            }
        ),
        AiTool(
            id = "resume",
            title = "Résumé de texte",
            category = ToolCategory.ECRITURE,
            icon = "📋",
            description = "Condensez des articles, rapports ou notes en points clés.",
            fields = listOf(
                AiToolField("texte", "Texte ou document à résumer", "Collez le texte complet...", isMultiLine = true),
                AiToolField("format", "Format du résumé", "Points clés, Synthèse rapide (TL;DR), Synthèse exécutive", defaultValue = "Points clés")
            ),
            promptTemplate = { params ->
                "Fournis un résumé clair et fidèle du texte suivant sous la forme : ${params["format"]}. Fais ressortir les enseignements capitaux :\n\n${params["texte"]}"
            }
        ),
        AiTool(
            id = "traduction",
            title = "Traduction multilingue",
            category = ToolCategory.ECRITURE,
            icon = "🌍",
            description = "Traduction précise et naturelle en français, anglais, espagnol, etc.",
            fields = listOf(
                AiToolField("texte", "Texte à traduire", "Votre texte...", isMultiLine = true),
                AiToolField("langue_cible", "Langue cible", "English, Français, Espagnol, Arabe, Chinois, Allemand", defaultValue = "English")
            ),
            promptTemplate = { params ->
                "Traduis fidèlement avec nuances contextuelles le texte suivant vers la langue cible : ${params["langue_cible"]}.\n\nTexte :\n${params["texte"]}"
            }
        ),

        // ======================= CRÉATION =======================
        AiTool(
            id = "prompt_generator",
            title = "Génération de prompts IA",
            category = ToolCategory.CREATION,
            icon = "⚡",
            description = "Concevez des prompts experts pour Midjourney, DALL-E ou Gemini.",
            fields = listOf(
                AiToolField("idee", "Votre idée de base", "Ex: Une métropole africaine futuriste avec gratte-ciels en verre"),
                AiToolField("moteur", "Outil ciblé", "Midjourney v6, DALL-E 3, Gemini, Stable Diffusion", defaultValue = "Midjourney v6")
            ),
            promptTemplate = { params ->
                "Génère 3 prompts ultra-détaillés et professionnels pour ${params["moteur"]} basés sur l'idée : ${params["idee"]}.\nInclus les paramètres de caméra, éclairage, ratio (ex: --ar 16:9), ambiance et niveau de rendu."
            }
        ),
        AiTool(
            id = "creation_publicite",
            title = "Publicité & Copywriting",
            category = ToolCategory.CREATION,
            icon = "📢",
            description = "Rédigez des annonces percutantes pour Facebook Ads, Google Ads ou affiches.",
            fields = listOf(
                AiToolField("produit", "Produit ou service", "Ex: Formation en ligne sur l'entrepreneuriat digital"),
                AiToolField("cible", "Public cible", "Ex: Jeunes diplômés et professionnels en reconversion"),
                AiToolField("objectif", "Objectif de la publicité", "Génération de leads, Vente directe, Notoriété", defaultValue = "Vente directe")
            ),
            promptTemplate = { params ->
                "Rédige une campagne publicitaire percutante pour : ${params["produit"]}.\nCible : ${params["cible"]}.\nObjectif : ${params["objectif"]}.\nFournis 3 accroches (hooks), le corps du texte persuasif avec la méthode AIDA et un appel à l'action irrésistible (CTA)."
            }
        ),
        AiTool(
            id = "scripts_video",
            title = "Scripts vidéo",
            category = ToolCategory.CREATION,
            icon = "🎬",
            description = "Scénarios complets pour vidéos courtes (Reels/Shorts) ou longues.",
            fields = listOf(
                AiToolField("sujet", "Sujet de la vidéo", "Ex: 3 habitudes quotidiennes pour décupler sa productivité"),
                AiToolField("duree", "Format / Durée", "Court 30s-60s (TikTok/Reel), Moyen 3-5min, Long 10min+", defaultValue = "Court 30s-60s (TikTok/Reel)")
            ),
            promptTemplate = { params ->
                "Écris le script vidéo complet et captivant sur : ${params["sujet"]} (${params["duree"]}).\nStructure requise : Hook d'accroche (0-3s), développement avec indications visuelles et voix-off, et fin avec CTA fort pour l'engagement."
            }
        ),
        AiTool(
            id = "storytelling",
            title = "Storytelling captivant",
            category = ToolCategory.CREATION,
            icon = "📖",
            description = "Racontez une histoire émotionnelle et inspirante pour votre marque ou projet.",
            fields = listOf(
                AiToolField("message", "Morale ou message principal", "Ex: Comment surmonter l'échec pour bâtir un empire"),
                AiToolField("protagoniste", "Personnage ou contexte", "Ex: Un jeune fondateur parti de zéro avec son smartphone")
            ),
            promptTemplate = { params ->
                "Rédige une histoire narrative saisissante selon la structure du Voyage du Héros.\nProtagoniste/Contexte : ${params["protagoniste"]}.\nMessage clé : ${params["message"]}.\nFais ressentir l'émotion et captive le lecteur dès la première phrase."
            }
        ),
        AiTool(
            id = "idees_contenu",
            title = "Idées de contenu viral",
            category = ToolCategory.CREATION,
            icon = "💡",
            description = "Générez des concepts de publications et sujets originaux.",
            fields = listOf(
                AiToolField("niche", "Votre secteur ou niche", "Ex: Économie, Mode, Tech, Gastronomie"),
                AiToolField("quantite", "Nombre d'idées", "5 idées fortes, 10 idées variées, 20 idées rapides", defaultValue = "10 idées variées")
            ),
            promptTemplate = { params ->
                "Génère ${params["quantite"]} innovantes et virales pour la niche : ${params["niche"]}.\nPour chaque idée : titre accrocheur, angle d'attaque différenciant et pourquoi le public cliquera ou partagera."
            }
        ),

        // ======================= BUSINESS =======================
        AiTool(
            id = "business_plan",
            title = "Business Plan complet",
            category = ToolCategory.BUSINESS,
            icon = "📊",
            description = "Structurez les piliers stratégiques et financiers de votre entreprise.",
            fields = listOf(
                AiToolField("nom_projet", "Nom et concept de l'entreprise", "Ex: Kori Express, livraison écologique par triporteurs solaires"),
                AiToolField("marche", "Zone géographique et marché cible", "Ex: Grandes villes d'Afrique de l'Ouest, commerces de proximité")
            ),
            promptTemplate = { params ->
                "En tant qu'expert en stratégie d'entreprise pour OBIN’SS IA, rédige une synthèse exécutive de Business Plan pour : ${params["nom_projet"]}.\nMarché cible : ${params["marche"]}.\nInclus : 1. Proposition de valeur, 2. Modèle de monétisation, 3. Analyse des barrières à l'entrée, 4. Stratégie de mise sur le marché, 5. Projections financières clés."
            }
        ),
        AiTool(
            id = "marketing_strategie",
            title = "Stratégie Marketing",
            category = ToolCategory.BUSINESS,
            icon = "🎯",
            description = "Élaborez votre plan d'acquisition client et positionnement.",
            fields = listOf(
                AiToolField("activite", "Votre activité / offre", "Ex: Application SaaS de comptabilité simplifiée pour indépendants"),
                AiToolField("budget", "Budget de démarrage", "Faible (Organique/Growth), Moyen, Conséquent", defaultValue = "Faible (Organique/Growth)")
            ),
            promptTemplate = { params ->
                "Élabore un plan marketing stratégique complet pour : ${params["activite"]}.\nContrainte budgétaire : ${params["budget"]}.\nDétaille les 4P (Produit, Prix, Place, Promotion), les personas clients, les canaux prioritaires d'acquisition et les indicateurs KPI à surveiller."
            }
        ),
        AiTool(
            id = "strategie_entreprise",
            title = "Stratégie d'entreprise",
            category = ToolCategory.BUSINESS,
            icon = "♟️",
            description = "Définissez une vision concurrentielle, des partenariats et des objectifs.",
            fields = listOf(
                AiToolField("entreprise", "Nom ou type d'entreprise", "Ex: Cabinet de conseil en transformation digitale"),
                AiToolField("defi", "Défi ou ambition majeure", "Ex: Passer de 5 à 50 clients grands comptes en 12 mois")
            ),
            promptTemplate = { params ->
                "Agis comme un conseiller en stratégie de direction pour OBIN’SS IA. Analyse l'entreprise : ${params["entreprise"]}.\nObjectif / Défi : ${params["defi"]}.\nFormule une stratégie d'expansion en 3 phases, avec gestion des risques et avantages concurrentiels durables."
            }
        ),
        AiTool(
            id = "etude_marche",
            title = "Étude de marché",
            category = ToolCategory.BUSINESS,
            icon = "📈",
            description = "Analysez la demande, la concurrence et les opportunités d'un secteur.",
            fields = listOf(
                AiToolField("secteur", "Secteur ou produit étudié", "Ex: Marché des cosmétiques naturels en Côte d'Ivoire et au Sénégal"),
                AiToolField("segments", "Segments de clientèle", "Ex: Femmes actives 25-45 ans, classes moyennes")
            ),
            promptTemplate = { params ->
                "Réalise une étude de marché synthétique et structurée pour le secteur : ${params["secteur"]}.\nSegments : ${params["segments"]}.\nInclus : taille estimée, tendances émergentes, analyse des forces concurrentielles de Porter et opportunités non exploitées."
            }
        ),
        AiTool(
            id = "analyse_swot",
            title = "Analyse SWOT",
            category = ToolCategory.BUSINESS,
            icon = "🧭",
            description = "Identifiez Forces, Faiblesses, Opportunités et Menaces.",
            fields = listOf(
                AiToolField("entreprise", "Entreprise ou projet", "Ex: Plateforme de téléconsultation médicale mobile")
            ),
            promptTemplate = { params ->
                "Fournis une analyse SWOT approfondie pour : ${params["entreprise"]}.\nPrésente les 4 cadrans distincts (Forces, Faiblesses, Opportunités, Menaces) et formule 3 initiatives stratégiques prioritaires d'action."
            }
        ),

        // ======================= RÉSEAUX SOCIAUX =======================
        AiTool(
            id = "tiktok_viral",
            title = "TikTok Viral & Hooks",
            category = ToolCategory.RESEAUX_SOCIAUX,
            icon = "📱",
            description = "Scripts de 30-60s avec accroches puissantes pour percer sur TikTok.",
            fields = listOf(
                AiToolField("niche", "Sujet ou niche de la vidéo", "Ex: 3 astuces d'IA pour gagner du temps au travail"),
                AiToolField("style", "Tonalité de la vidéo", "Éducatif rythmé, Humoristique, Storytime, Défi", defaultValue = "Éducatif rythmé")
            ),
            promptTemplate = { params ->
                "Crée un script TikTok à fort potentiel viral sur le sujet : ${params["niche"]}.\nStyle : ${params["style"]}.\nFournis :\n1. 3 accroches (hooks) irrésistibles de 3 secondes,\n2. Script étape par étape avec texte écran,\n3. Un call-to-action stimulant pour inciter à l'abonnement et au partage,\n4. 7 hashtags tendance optimisés pour l'algorithme."
            }
        ),
        AiTool(
            id = "facebook_post",
            title = "Publications Facebook",
            category = ToolCategory.RESEAUX_SOCIAUX,
            icon = "👥",
            description = "Posts engageants conçus pour maximiser les réactions et partages.",
            fields = listOf(
                AiToolField("sujet", "Thème de la publication", "Ex: Conseils pour lancer un commerce local rentable"),
                AiToolField("objectif", "Objectif principal", "Partages et commentaires, Clics vers lien, Notoriété", defaultValue = "Partages et commentaires")
            ),
            promptTemplate = { params ->
                "Rédige un post Facebook percutant et engageant sur : ${params["sujet"]}.\nObjectif : ${params["objectif"]}.\nUtilise une accroche intrigante, des paragraphes aérés avec emojis adaptés et termine par une question ouverte invitant chaque lecteur à donner son avis."
            }
        ),
        AiTool(
            id = "instagram_post",
            title = "Post & Carrousel Instagram",
            category = ToolCategory.RESEAUX_SOCIAUX,
            icon = "📸",
            description = "Légendes captivantes et découpage de carrousels pédagogiques.",
            fields = listOf(
                AiToolField("theme", "Thème du post ou carrousel", "Ex: 5 étapes pour vaincre la procrastination"),
                AiToolField("format", "Format", "Carrousel 7 slides, Post image unique, Légende longue", defaultValue = "Carrousel 7 slides")
            ),
            promptTemplate = { params ->
                "Rédige le contenu complet d'un ${params["format"]} Instagram sur : ${params["theme"]}.\nDétaille le contenu exact de chaque slide (titre + texte court) ainsi que la légende complète d'accompagnement avec appel aux likes, enregistrements et hashtags."
            }
        ),
        AiTool(
            id = "youtube_script",
            title = "Script & Titres YouTube",
            category = ToolCategory.RESEAUX_SOCIAUX,
            icon = "▶️",
            description = "Titres à fort taux de clic (CTR), miniature conceptuelle et script.",
            fields = listOf(
                AiToolField("sujet", "Sujet de la vidéo YouTube", "Ex: Comment débuter en programmation en 2026"),
                AiToolField("duree", "Durée visée", "5 à 8 minutes, 10 à 15 minutes, 20+ minutes", defaultValue = "10 à 15 minutes")
            ),
            promptTemplate = { params ->
                "Pour une vidéo YouTube sur le sujet : ${params["sujet"]} (${params["duree"]}) :\n1. Propose 5 titres percutants à haut CTR (sans putaclic excessif),\n2. Décris l'idée visuelle de la miniature (thumbnail),\n3. Rédige l'introduction captivante (les 30 premières secondes déterminantes),\n4. Donne le plan détaillé de la vidéo avec chapitrage."
            }
        ),

        // ======================= ÉTUDES =======================
        AiTool(
            id = "explication_cours",
            title = "Explication de cours",
            category = ToolCategory.ETUDES,
            icon = "📖",
            description = "Comprenez les concepts scolaires ou universitaires avec la méthode Feynman.",
            fields = listOf(
                AiToolField("concept", "Concept, théorème ou notion à expliquer", "Ex: La loi de l'offre et de la demande, La relativité restreinte, L'algorithme de Dijkstra"),
                AiToolField("niveau", "Votre niveau d'études", "Collège, Lycée, Université / Supérieur, Débutant complet", defaultValue = "Université / Supérieur")
            ),
            promptTemplate = { params ->
                "Tu es un professeur pédagogue bienveillant pour OBIN’SS IA. Explique de manière limpide : ${params["concept"]}.\nNiveau : ${params["niveau"]}.\nUtilise une analogie de la vie quotidienne, la définition rigoureuse, un exemple concret d'application et une question de contrôle des acquis."
            }
        ),
        AiTool(
            id = "fiches_revision",
            title = "Fiches de révision",
            category = ToolCategory.ETUDES,
            icon = "📑",
            description = "Fiches synthétiques avec définitions clés, formules et mnémoniques.",
            fields = listOf(
                AiToolField("sujet", "Matière ou chapitre à réviser", "Ex: La guerre froide, Les fonctions dérivées en mathématiques, Le système circulatoire humain")
            ),
            promptTemplate = { params ->
                "Conçois une fiche de révision ultra-synthétique et visuelle sur : ${params["sujet"]}.\nInclus : 1. Les définitions essentielles à mémoriser, 2. Les formules ou dates charnières, 3. Les erreurs courantes d'examen à éviter, 4. Une astuce mnémotechnique."
            }
        ),
        AiTool(
            id = "quiz_revision",
            title = "Quiz & QCM d'entraînement",
            category = ToolCategory.ETUDES,
            icon = "❓",
            description = "Testez vos connaissances avec des QCM et leurs explications détaillées.",
            fields = listOf(
                AiToolField("matiere", "Matière ou sujet du test", "Ex: Droit constitutionnel, Biologie cellulaire, Histoire contemporaine"),
                AiToolField("nb_questions", "Nombre de questions", "5 questions, 10 questions", defaultValue = "5 questions")
            ),
            promptTemplate = { params ->
                "Génère un quiz d'évaluation de ${params["nb_questions"]} questions QCM à 4 choix (A, B, C, D) sur : ${params["matiere"]}.\nPlace le corrigé détaillé à la fin avec l'explication précise de chaque bonne réponse."
            }
        ),
        AiTool(
            id = "exercices_pratiques",
            title = "Génération d'exercices",
            category = ToolCategory.ETUDES,
            icon = "✏️",
            description = "Créez des exercices graduels adaptés à votre programme.",
            fields = listOf(
                AiToolField("theme", "Thématique de l'exercice", "Ex: Équations différentielles du premier ordre, Analyse financière du bilan"),
                AiToolField("difficulte", "Difficulté", "Facile (Découverte), Moyen (Standard examen), Difficile (Perfectionnement)", defaultValue = "Moyen (Standard examen)")
            ),
            promptTemplate = { params ->
                "Propose un énoncé d'exercice complet et réaliste sur : ${params["theme"]}.\nDifficulté : ${params["difficulte"]}.\nFournis d'abord l'énoncé clairement formulé avec les données, puis propose des indices méthodologiques pour guider l'élève."
            }
        ),
        AiTool(
            id = "correction_exercice",
            title = "Correction d'exercice",
            category = ToolCategory.ETUDES,
            icon = "✅",
            description = "Obtenez un corrigé pas-à-pas avec méthode de résolution.",
            fields = listOf(
                AiToolField("enonce", "Énoncé de l'exercice ou question", "Collez l'énoncé complet...", isMultiLine = true),
                AiToolField("reponse_eleve", "Votre tentative de réponse (optionnel)", "Collez votre brouillon...", isMultiLine = true)
            ),
            promptTemplate = { params ->
                "Corrige l'exercice suivant avec une rigueur pédagogique totale :\n\nÉnoncé :\n${params["enonce"]}\n\nTentative de l'élève :\n${params["reponse_eleve"]}\n\nFournis la solution détaillée pas-à-pas, souligne les erreurs de raisonnement commises s'il y en a et donne le barème indicatif de notation."
            }
        ),

        // ======================= PROGRAMMATION =======================
        AiTool(
            id = "code_generator",
            title = "Génération de code",
            category = ToolCategory.PROGRAMMATION,
            icon = "💻",
            description = "Rédigez du code propre en Kotlin, Python, TypeScript, Java, SQL, etc.",
            fields = listOf(
                AiToolField("langage", "Langage", "Kotlin, Python, TypeScript, JavaScript, Java, C++, PHP, SQL, Rust, Go", defaultValue = "Kotlin"),
                AiToolField("tache", "Que doit accomplir ce programme ?", "Ex: Une fonction de tri d'objets avec recherche binaire et gestion d'erreurs", isMultiLine = true)
            ),
            promptTemplate = { params ->
                "Tu es un architecte logiciel principal pour OBIN’SS IA. Rédige le code complet, robuste et propre en ${params["langage"]} pour répondre à ce besoin :\n${params["tache"]}\n\nFormate le code dans un bloc markdown avec syntaxe surlignée, commente les fonctions clés, précise la complexité temporelle/spatiale O(n) et donne un exemple d'exécution."
            }
        ),
        AiTool(
            id = "code_debugger",
            title = "Correction de code",
            category = ToolCategory.PROGRAMMATION,
            icon = "🐛",
            description = "Détectez les bugs, réparez les erreurs et optimisez vos scripts.",
            fields = listOf(
                AiToolField("langage", "Langage", "JavaScript, Python, Kotlin, Java, TypeScript, C++, PHP, SQL", defaultValue = "JavaScript"),
                AiToolField("code", "Code source avec bug", "Collez votre code ici...", isMultiLine = true),
                AiToolField("erreur", "Message d'erreur reçu", "Ex: NullPointerException, TypeError: undefined is not a function")
            ),
            promptTemplate = { params ->
                "Analyse ce programme ${params["langage"]} générant l'anomalie : '${params["erreur"]}'.\n\nCode :\n${params["code"]}\n\n1. Identifie la cause racine exacte du problème,\n2. Fournis le code corrigé complet et prêt à l'exécution,\n3. Explique la règle de bonne pratique pour éviter que ce bug ne réapparaisse."
            }
        ),
        AiTool(
            id = "explication_code",
            title = "Explication de code",
            category = ToolCategory.PROGRAMMATION,
            icon = "🔍",
            description = "Comprenez ligne par ligne le fonctionnement d'un algorithme.",
            fields = listOf(
                AiToolField("langage", "Langage", "Python, Kotlin, TypeScript, Java, C++, Rust", defaultValue = "Python"),
                AiToolField("code", "Code à déchiffrer", "Collez le morceau de code...", isMultiLine = true)
            ),
            promptTemplate = { params ->
                "Décompose et explique avec limpidité le fonctionnement de ce code ${params["langage"]} :\n\n${params["code"]}\n\nExplique le rôle de chaque variable et boucle, la logique générale de l'algorithme, et donne une analogie pour bien comprendre son fonctionnement."
            }
        )
    )
}
