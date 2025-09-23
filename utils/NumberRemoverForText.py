import re


def remove_number_text(text):
    text = re.sub('[0-9]+', ' ', text)
    return text
