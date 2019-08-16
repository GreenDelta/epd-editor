
tooltips = [
    ("All", "General information"),
    ("All", "UUID"),
    ("All", "File"),
    ("All", "Classification"),

    ("EPD", "Name"),
    ("EPD", "Further properties"),
    ("EPD", "Synonyms"),
    ("EPD", "Comment"),
    ("EPD", "Declared product"),
    ("EPD", "Product amount"),
    ("EPD", "Product unit"),
    ("EPD", "External documentation sources"),
    ("EPD", "Uncertainty margins"),
    ("EPD", "Uncertainty margins description"),
    ("EPD", "Time"),
    ("EPD", "Reference year"),
    ("EPD", "Valid until"),
    ("EPD", "Time description"),
    ("EPD", "Geography"),
    ("EPD", "Location"),
    ("EPD", "Geography description"),
    ("EPD", "Technology"),
    ("EPD", "Technology description"),
    ("EPD", "Technical prupose"),
    ("EPD", "Pictogram"),
    ("EPD", "Flow diagrams or pictures"),
    ("EPD", "Modelling and validation"),
    ("EPD", "Subtype"),
    ("EPD", "Use advice"),
    ("EPD", "LCA method details"),
    ("EPD", "Data sources"),
    ("EPD", "Compliance declarations"),
    ("EPD", "Review"),
    ("EPD", "Review type"),
    ("EPD", "Review details"),
    ("EPD", "Review report"),
    ("EPD", "Reviewer"),
    ("EPD", "Data entry"),
    ("EPD", "Last update"),
    ("EPD", "Documentor"),
    ("EPD", "Data formats"),
    ("EPD", "Publication and ownership"),
    ("EPD", "Data set version"),
    ("EPD", "Owner"),
    ("EPD", "Copyright"),
    ("EPD", "Access restrictions"),
    ("EPD", "EPD profile"),
    ("EPD", "Scenarios"),
    ("EPD", "Modules"),
    ("EPD", "Results"),
]


def make_key(tooltip):
    k = tooltip[0] + "_"
    label = tooltip[1]
    for part in label.split(" "):  # type: str
        p = part.strip()
        k += p[0].upper() + p[1:]
    return k


if __name__ == "__main__":

    print("Java fields:\n\n")
    context = None
    for pair in tooltips:
        if pair[0] != context:
            context = pair[0]
            print("\n\t// %s" % context)
        k = make_key(pair)
        print("\tpublic static String %s;" % k)

    print("\n\nMessages:\n\n")
    for pair in tooltips:
        k = make_key(pair)
        print("%s = %s" % (k, pair[1]))
