from .Abreviation_Slang import other_clean
from .Contractions import main_contraction
from .EmojiRemover import remove_emoji
from .PunctuationRemover import remove_punc
from .NumberRemoverForText import remove_number_text
from .LowerCaracter import to_lower
from .URLRemover import remove_URL


def text_modificator(TextExtracted):
    TextExtracted = to_lower(TextExtracted)
    TextExtracted = remove_URL(TextExtracted)
    TextExtracted = remove_number_text(TextExtracted)
    TextExtracted = remove_emoji(TextExtracted)
    TextExtracted = remove_punc(TextExtracted)
    TextExtracted = main_contraction(TextExtracted)
    TextExtracted = other_clean(TextExtracted)
    return TextExtracted
