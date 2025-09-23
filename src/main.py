from src import MLrecognition
from src.ExtractTextFromImg import image_to_text
from src.Translator import Translate
import os
import sys

# Add parent directory to path to enable imports from utils
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

def main():
    # Since textscanner.py is missing, we'll comment out that call for now
    # textscanner()
    TextExtracted = image_to_text('../data/ImageWithText.jpg')
    MLrecognition.prediction(TextExtracted)
    Translate(TextExtracted)


if __name__ == "__main__":
    main()
