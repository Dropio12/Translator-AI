# Translator-AI

A Python-based AI translator application that extracts text from images and provides language detection and translation capabilities.

## Project Structure

```
Translator-AI/
├── src/                    # Main source code
│   ├── main.py            # Application entry point
│   ├── MLrecognition.py   # Machine learning language recognition
│   ├── Translator.py      # Translation functionality
│   ├── ExtractTextFromImg.py  # Text extraction from images
│   ├── frontends.py       # Frontend UI implementation
│   └── d.py               # Additional UI components
├── utils/                 # Utility functions and text processing
│   ├── TextExtractedModificator.py  # Text modification pipeline
│   ├── Abreviation_Slang.py        # Abbreviation and slang processing
│   ├── Contractions.py             # Contraction expansion
│   ├── EmojiRemover.py             # Emoji removal
│   ├── LowerCaracter.py            # Text lowercasing
│   ├── NumberRemover.py            # Number removal
│   ├── NumberRemoverForText.py     # Text-specific number removal
│   ├── PunctuationRemover.py       # Punctuation removal
│   └── URLRemover.py               # URL removal
├── data/                  # Datasets and sample images
│   ├── dataset.csv
│   ├── language-identification-datasets.csv
│   ├── ImageWithText.jpg
│   ├── IMG.png
│   └── IMG_20221113_114712.png
├── assets/                # UI assets
│   └── frontend.kv        # Kivy UI layout file
├── models/                # Machine learning models (empty)
├── tests/                 # Unit and integration tests (empty)
├── docs/                  # Documentation (empty)
└── README.md             # This file
```

## Features

- **Image Text Extraction**: Extract text from images using OCR (Optical Character Recognition)
- **Language Detection**: Automatically detect the language of extracted text using machine learning
- **Translation**: Translate text to different languages
- **GUI Interface**: User-friendly interface built with Kivy/KivyMD
- **Text Processing**: Comprehensive text cleaning and preprocessing utilities

## Installation

### Prerequisites

- Python 3.6+
- Tesseract OCR engine
- Required Python packages (install via pip):

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

### Tesseract Installation

#### Windows
Download and install Tesseract from: https://github.com/UB-Mannheim/tesseract/wiki

#### Linux (Ubuntu/Debian)
```bash
sudo apt-get install tesseract-ocr
```

#### macOS
```bash
brew install tesseract
```

## Usage

### Command Line
Run the main application:
```bash
cd src/
python main.py
```

### GUI Application
Run the GUI frontend:
```bash
cd src/
python frontends.py
```

## How It Works

1. **Text Extraction**: The application uses OpenCV and Tesseract to extract text from images
2. **Text Processing**: Raw extracted text is cleaned using various utility functions:
   - Convert to lowercase
   - Remove URLs, numbers, punctuation
   - Remove emojis
   - Expand contractions
   - Process abbreviations and slang
3. **Language Detection**: Uses machine learning (Decision Tree Classifier) to identify the language
4. **Translation**: Uses Google Translate API to translate text to the target language

## Configuration

- Update the Tesseract executable path in `src/ExtractTextFromImg.py` if needed
- Modify language datasets in the `data/` directory
- Customize UI layouts in `assets/frontend.kv`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add your changes
4. Add tests in the `tests/` directory
5. Submit a pull request

## License

This project is open source. Please check the repository for license details.

## Known Issues

- `textscanner.py` module is referenced but not present in the codebase
- Hardcoded Tesseract path may need adjustment for different systems
- Some imports may need adjustment based on your Python environment

## Future Enhancements

- Add comprehensive unit tests
- Implement the missing `textscanner` module
- Add support for more image formats
- Improve language detection accuracy
- Add more translation services
- Containerize the application