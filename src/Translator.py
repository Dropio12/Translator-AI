from googletrans import Translator


def Translate(text):
    translator = Translator()
    language_des = input("Enter the language you want to translate to: ")
    print(translator.translate(text, dest=language_des).text)
