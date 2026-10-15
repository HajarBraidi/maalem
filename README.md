# Maalem

Application mobile Android de mise en relation entre artisans et particuliers au Maroc.

Projet de fin d'année (PFA, 2e année) réalisé à l'ENSIAS, année 2025-2026.

## Contexte

Trouver un artisan fiable et décrire correctement une panne n'est pas toujours simple. Maalem permet à un citoyen de publier une demande de service avec photo, de recevoir des offres d'artisans, de comparer ces offres puis d'échanger avec l'artisan choisi. Un modèle d'intelligence artificielle embarqué dans l'application aide à identifier le type de problème à partir de la photo.

## Acteurs et fonctionnalités

**Citoyen**
- Inscription et connexion
- Recherche d'artisans par spécialité et par ville, avec carte
- Création d'une demande de service avec photo et localisation
- Proposition automatique de la catégorie grâce au modèle d'IA
- Consultation et comparaison des offres reçues
- Messagerie avec les artisans
- Avis et notes après une intervention

**Artisan**
- Inscription, puis validation du compte par l'administrateur
- Consultation des demandes correspondant à son métier
- Envoi d'offres et suivi de leur état
- Gestion du profil professionnel

**Administrateur**
- Tableau de bord avec statistiques de la plateforme
- Validation des inscriptions d'artisans
- Gestion des comptes, des catégories et des villes
- Envoi de notifications

## Modèle d'IA embarqué

Le modèle classe une photo de mur en 4 catégories : `fissure`, `peinture`, `humidite` et `autre`. La classe `autre` (murs sains et images hors sujet) permet de rejeter les photos qui ne correspondent à aucun défaut ; l'utilisateur choisit alors la catégorie lui-même.

| Classe du modèle | Spécialité proposée |
|---|---|
| fissure | Maçonnerie |
| peinture | Peinture |
| humidite | Étanchéité |
| autre | Saisie manuelle |

- Jeu de données : images de défauts de bâtiment (Roboflow) complétées par des images hors domaine, soit 6 779 images d'entraînement, 1 451 de validation et 1 457 de test
- Modèles comparés : CNN de base, MobileNetV2, EfficientNetB0, ResNet50
- Modèle retenu : **MobileNetV2** affiné, **96,1 % d'exactitude** sur l'ensemble de test, pour une taille d'environ **2,5 Mo**
- Exécution sur le téléphone avec TensorFlow Lite (LiteRT), entrée 224 x 224, sans connexion à un serveur

Le modèle se trouve dans `app/src/main/assets/maalem_model.tflite` et son intégration dans `app/src/main/java/com/example/maalem/ml/WallDefectClassifier.kt`.

## Technologies

- **Langage** : Kotlin, interfaces en XML
- **Architecture** : MVVM avec séparation en couches (presentation, domain, data), injection de dépendances avec Hilt
- **Backend** : Firebase Authentication, Cloud Firestore, Realtime Database, Firebase Storage, Firebase Cloud Messaging
- **Carte** : osmdroid (OpenStreetMap)
- **Images** : Glide et Coil
- **IA** : TensorFlow Lite / LiteRT, MobileNetV2
- **Build** : Gradle (Kotlin DSL), Docker pour un environnement de compilation reproductible

## Structure du projet

```
app/src/main/java/com/example/maalem/
├── data/          modèles de données et implémentations des repositories (Firebase)
├── domain/        interfaces des repositories et cas d'utilisation
├── presentation/  écrans et ViewModels (auth, citizen, artisan, admin, chat)
├── ml/            classifieur TensorFlow Lite
├── di/            modules Hilt
└── utils/         fonctions utilitaires (localisation)
```

## Installation

Prérequis : Android Studio, JDK 11, un projet Firebase.

1. Cloner le dépôt :
   ```bash
   git clone https://github.com/HajarBraidi/maalem.git
   ```
2. Créer un projet Firebase, ajouter une application Android avec le package `com.example.maalem`, puis placer le fichier `google-services.json` dans le dossier `app/`.
3. Activer Authentication (email / mot de passe), Firestore, Realtime Database et Storage dans la console Firebase.
4. Ouvrir le projet dans Android Studio et lancer l'application sur un émulateur ou un téléphone (Android 8.0, API 26 minimum).

Compilation avec Docker :

```bash
docker compose run android-build ./gradlew assembleDebug
```

## Équipe

- Hajar Braidi
- Khadija Ait Oufkir

Encadrante : Pr. Laila Cheikhi

## Licence

Ce projet est distribué sous licence MIT. Voir le fichier `LICENSE`.
