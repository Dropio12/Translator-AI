# Translator AI

An Android app that reads the words in a photo, finds out which language they are in, and translates them into the language you pick. The translation is drawn on the photo, in the place of the original words.

Everything runs on the phone. There is no large language model and no paid API.

## What it does

- Get an image: take a photo with the camera, pick one from the gallery, or open the sample (a French menu made for this repo).
- Live mode: point the camera at some text and the translations are drawn over the camera preview.
- Each block of text gets a patch in the color of the background around it, and the translation is written on the patch, as big as fits. Tilted text gets a tilted patch.
- A switch shows the original photo again. Under the photo, a list shows each original block and its translation.
- You pick the target language. The source language is detected, or you can pick it yourself.

## How it works

1. **Read the text.** [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition/v2/android) finds the blocks of text and the 4 corners of each block. The app uses the bundled Latin model, so it does not need Google Play services.
2. **Detect the language.** [ML Kit Language Identification](https://developers.google.com/ml-kit/language/identification/android) looks at all the text of the image at once. One short word is hard to identify, a whole sign is easier.
3. **Translate.** [ML Kit on-device Translation](https://developers.google.com/ml-kit/language/translation/android) translates each block. The first time a language is used, its model is downloaded and the app says so. After that, translation works offline.

Then the app draws the result. The 4 corners of a block give a box with a center, a size and an angle. The font size is the biggest one that still fits the box, found with a binary search. Lines break between words, or between characters for languages written without spaces (Chinese, Japanese, Thai). The patch color is the median color of a thin ring of pixels just outside the block.

## Code layout

```
app/src/main/java/io/github/dropio12/translatorai/
  domain/  plain Kotlin, no Android code: box geometry, text fitting, patch colors,
           language codes, and the detect then translate flow (a Kotlin Flow)
  data/    ML Kit and CameraX: text reader, language detector, translator,
           image loading, live camera analyzer
  ui/      Jetpack Compose screens, the overlay, and the ViewModel
app/src/test/  JUnit tests for the domain package
```

Stack: Kotlin 2.4, Jetpack Compose with Material 3, Kotlin coroutines and Flow, CameraX 1.6, ML Kit, Android Gradle Plugin 9.5, Gradle 9.8.

## Build and run

You need JDK 17 or newer and the Android SDK with platform 36 and build tools 36.1.0. Point Gradle to the SDK with `ANDROID_HOME` or a `local.properties` file containing `sdk.dir=/path/to/Android/Sdk`.

```
./gradlew assembleDebug testDebugUnitTest
./gradlew installDebug
```

`installDebug` needs a phone or an emulator connected. On Windows, use `gradlew.bat`.

## What it assumes

- Android 9 (API 28) or newer.
- Text written in the Latin alphabet (English, French, Spanish, German and so on). Other alphabets would need the other ML Kit text recognition models.
- An internet connection the first time each language is used, to download its translation model.
- One language per image: the language is detected on all the text together.
- Translations come from ML Kit's on-device models, which Google describes as meant for casual and simple translations.
- In live mode the app does not have the camera pixels, so the patches are dark instead of matching the background, and they are drawn from the last frame that finished, so they can trail a moving camera a little.

## History

The first version (2022) was a Python and Kivy app that used OpenCV, Tesseract, a scikit-learn language model and googletrans. It is kept at the tag `v0-python`.
