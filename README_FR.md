# Translator-AI

Une application de traduction basée sur l'intelligence artificielle qui extrait du texte à partir d'images et offre des fonctionnalités de détection et de traduction de langues.

## 📋 Table des matières

- [Aperçu du projet](#aperçu-du-projet)
- [Fonctionnalités](#fonctionnalités)
- [Structure du projet](#structure-du-projet)
- [Installation](#installation)
- [Utilisation](#utilisation)
- [Fonctionnement](#fonctionnement)
- [Configuration](#configuration)
- [Contribution](#contribution)
- [Problèmes connus](#problèmes-connus)
- [Améliorations futures](#améliorations-futures)
- [Licence](#licence)

## 🎯 Aperçu du projet

Translator-AI est une application Python sophistiquée qui combine la vision par ordinateur, l'apprentissage automatique et les services de traduction pour offrir une solution complète d'extraction et de traduction de texte à partir d'images. Que vous ayez besoin de traduire du texte dans une photo, un document numérisé ou une capture d'écran, cette application facilite le processus de bout en bout.

## ✨ Fonctionnalités

- **📸 Extraction de texte à partir d'images** : Extraction précise de texte à partir d'images en utilisant la technologie OCR (Reconnaissance Optique de Caractères) via Tesseract
- **🌍 Détection automatique de langue** : Identifie automatiquement la langue du texte extrait grâce à l'apprentissage automatique
- **🔄 Traduction multilingue** : Traduit le texte vers différentes langues en utilisant l'API Google Translate
- **🖥️ Interface graphique intuitive** : Interface utilisateur moderne et conviviale construite avec Kivy/KivyMD
- **🧹 Prétraitement de texte** : Suite complète d'utilitaires de nettoyage et de prétraitement de texte incluant :
  - Suppression des URLs
  - Suppression des nombres et de la ponctuation
  - Suppression des emojis
  - Expansion des contractions
  - Traitement des abréviations et de l'argot
  - Normalisation de la casse

## 📁 Structure du projet

```
Translator-AI/
├── src/                    # Code source principal
│   ├── main.py            # Point d'entrée de l'application
│   ├── MLrecognition.py   # Reconnaissance de langue par apprentissage automatique
│   ├── Translator.py      # Fonctionnalité de traduction
│   ├── ExtractTextFromImg.py  # Extraction de texte à partir d'images
│   ├── frontends.py       # Implémentation de l'interface utilisateur
│   └── d.py               # Composants UI additionnels
├── utils/                 # Fonctions utilitaires et traitement de texte
│   ├── TextExtractedModificator.py  # Pipeline de modification de texte
│   ├── Abreviation_Slang.py        # Traitement des abréviations et de l'argot
│   ├── Contractions.py             # Expansion des contractions
│   ├── EmojiRemover.py             # Suppression des emojis
│   ├── LowerCaracter.py            # Conversion en minuscules
│   ├── NumberRemover.py            # Suppression des nombres
│   ├── NumberRemoverForText.py     # Suppression des nombres dans le texte
│   ├── PunctuationRemover.py       # Suppression de la ponctuation
│   └── URLRemover.py               # Suppression des URLs
├── data/                  # Jeux de données et images d'exemple
│   ├── dataset.csv
│   ├── language-identification-datasets.csv
│   ├── ImageWithText.jpg
│   ├── IMG.png
│   └── IMG_20221113_114712.png
├── assets/                # Ressources UI
│   └── frontend.kv        # Fichier de mise en page Kivy
├── models/                # Modèles d'apprentissage automatique
├── tests/                 # Tests unitaires et d'intégration
├── docs/                  # Documentation
└── README.md             # Documentation principale (en anglais)
```

## 🚀 Installation

### Prérequis

Avant de commencer, assurez-vous d'avoir installé :

- **Python 3.6 ou supérieur** : [Télécharger Python](https://www.python.org/downloads/)
- **Tesseract OCR** : Moteur de reconnaissance de caractères

### Installation des dépendances Python

#### Option 1 : Installation via requirements.txt (recommandée)

```bash
pip install -r requirements.txt
```

#### Option 2 : Installation manuelle des packages

```bash
pip install opencv-python
pip install pytesseract
pip install pandas
pip install scikit-learn
pip install googletrans==3.1.0a0
pip install kivy
pip install kivymd
pip install pillow
```

### Installation de Tesseract OCR

#### Windows

1. Téléchargez l'installateur depuis : [Tesseract pour Windows](https://github.com/UB-Mannheim/tesseract/wiki)
2. Exécutez l'installateur et suivez les instructions
3. Notez le chemin d'installation (généralement `C:\Program Files\Tesseract-OCR`)

#### Linux (Ubuntu/Debian)

```bash
sudo apt-get update
sudo apt-get install tesseract-ocr
sudo apt-get install tesseract-ocr-fra  # Pour le support du français
```

#### macOS

```bash
brew install tesseract
```

## 💻 Utilisation

### Application en ligne de commande

Pour exécuter l'application principale :

```bash
cd src/
python main.py
```

### Application avec interface graphique

Pour lancer l'interface graphique complète :

```bash
cd src/
python frontends.py
```

### Exemple d'utilisation

1. Lancez l'application
2. Sélectionnez une image contenant du texte
3. Le texte sera automatiquement extrait et affiché
4. La langue du texte sera détectée automatiquement
5. Choisissez la langue cible pour la traduction
6. Obtenez votre traduction instantanément !

## ⚙️ Fonctionnement

L'application fonctionne en suivant un pipeline de traitement en plusieurs étapes :

### 1. Extraction de texte
- Utilise **OpenCV** pour le prétraitement de l'image
- Applique **Tesseract OCR** pour extraire le texte brut de l'image
- Gère différents formats d'images (JPG, PNG, etc.)

### 2. Prétraitement du texte
Le texte extrait est nettoyé en utilisant plusieurs utilitaires :
- **Normalisation** : Conversion en minuscules pour uniformiser le texte
- **Suppression d'éléments** : URLs, nombres, ponctuation, emojis
- **Expansion** : Contractions (par ex. "n't" → "not")
- **Traitement spécialisé** : Abréviations et expressions argotiques

### 3. Détection de langue
- Utilise un modèle d'apprentissage automatique (**Decision Tree Classifier**)
- S'entraîne sur des jeux de données multilingues
- Identifie automatiquement la langue source du texte

### 4. Traduction
- Intègre l'**API Google Translate**
- Traduit le texte nettoyé vers la langue cible choisie
- Supporte de nombreuses paires de langues

## 🔧 Configuration

### Chemin de Tesseract

Si Tesseract n'est pas dans votre PATH système, vous devrez peut-être mettre à jour le chemin dans `src/ExtractTextFromImg.py` :

```python
pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'  # Windows
# ou
pytesseract.pytesseract.tesseract_cmd = r'/usr/bin/tesseract'  # Linux/macOS
```

### Jeux de données de langues

Pour améliorer la détection de langue, vous pouvez :
- Modifier les jeux de données dans le répertoire `data/`
- Ajouter de nouveaux échantillons de langues au fichier `language-identification-datasets.csv`

### Personnalisation de l'interface

Les mises en page de l'interface utilisateur peuvent être personnalisées dans `assets/frontend.kv` en utilisant le langage KV de Kivy.

## 🤝 Contribution

Les contributions sont les bienvenues ! Voici comment vous pouvez contribuer :

1. **Forkez** le dépôt
2. **Créez** une branche pour votre fonctionnalité (`git checkout -b feature/AmazingFeature`)
3. **Commitez** vos changements (`git commit -m 'Ajout d'une fonctionnalité géniale'`)
4. **Ajoutez** des tests dans le répertoire `tests/` si applicable
5. **Poussez** vers la branche (`git push origin feature/AmazingFeature`)
6. **Ouvrez** une Pull Request

### Directives de contribution

- Suivez le style de code existant
- Ajoutez des commentaires pour le code complexe
- Mettez à jour la documentation si nécessaire
- Assurez-vous que tous les tests passent

## ⚠️ Problèmes connus

- Le module `textscanner.py` est référencé mais absent du code source
- Le chemin Tesseract peut nécessiter un ajustement selon votre système
- Certaines importations peuvent nécessiter des ajustements selon votre environnement Python
- Les performances peuvent varier selon la qualité de l'image d'entrée
- La précision de la traduction dépend de l'API Google Translate

## 🚧 Améliorations futures

### Tests et qualité du code
- [ ] Ajouter des tests unitaires complets
- [ ] Implémenter des tests d'intégration
- [ ] Améliorer la couverture de code

### Fonctionnalités
- [ ] Implémenter le module `textscanner` manquant
- [ ] Ajouter le support de formats d'images supplémentaires (TIFF, BMP, WebP)
- [ ] Améliorer la précision de la détection de langue
- [ ] Intégrer des services de traduction alternatifs
- [ ] Ajouter le support de la traduction par lots
- [ ] Implémenter la sauvegarde de l'historique des traductions

### Infrastructure
- [ ] Containeriser l'application avec Docker
- [ ] Créer une API REST pour l'intégration
- [ ] Ajouter le support multi-plateforme amélioré
- [ ] Optimiser les performances pour les grandes images

## 📄 Licence

Ce projet est open source. Veuillez consulter le dépôt pour les détails de la licence.

---

## 📞 Support

Pour toute question ou problème :
- Ouvrez une issue sur GitHub
- Consultez la documentation existante
- Rejoignez notre communauté de contributeurs

**Développé avec ❤️ par la communauté Translator-AI**
