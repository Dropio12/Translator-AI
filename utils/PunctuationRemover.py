import re


def remove_punc(text):
    text = re.sub(r'[^\w\s]', ' ', text)
    return text
