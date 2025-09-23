def remove_number(text):
    output = ''.join(c for c in text if not c.isdigit())
    return output
