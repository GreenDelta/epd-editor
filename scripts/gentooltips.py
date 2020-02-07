
tooltips = [
    ("All", "General information"),
    ("All", "UUID"),
    ("All", "File"),
    ("All", "Classification"),
    ("All", "Administrative information"),
    ("All", "Last update"),
    ("All", "Data set version"),

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
    ("EPD", "Documentor"),
    ("EPD", "Data formats"),
    ("EPD", "Publication and ownership"),
    ("EPD", "Owner"),
    ("EPD", "Copyright"),
    ("EPD", "Access restrictions"),
    ("EPD", "EPD profile"),
    ("EPD", "Scenarios"),
    ("EPD", "Modules"),
    ("EPD", "Results"),

    ("Flow", "Name"),
    ("Flow", "Synonyms"),
    ("Flow", "Description"),
    ("Flow", "Generic product"),
    ("Flow", "Vendor information"),
    ("Flow", "Is vendor specific"),
    ("Flow", "Vendor"),
    ("Flow", "Vendor documentation"),
    ("Flow", "Flow properties"),
    ("Flow", "Material properties"),

    ("Contact", "Short name"),
    ("Contact", "Name"),
    ("Contact", "Address"),
    ("Contact", "Telephone"),
    ("Contact", "Telefax"),
    ("Contact", "Website"),
    ("Contact", "Logo"),

    ("Source", "Short name"),
    ("Source", "Citation"),
    ("Source", "Description"),
    ("Source", "Logo"),
    ("Source", "Belongs to"),
    ("Source", "Links to external files"),

    ("FlowProperty", "Name"),
    ("FlowProperty", "Synonyms"),
    ("FlowProperty", "Description"),
    ("FlowProperty", "UnitGroup"),

    ("UnitGroup", "Name"),
    ("UnitGroup", "Description"),
    ("UnitGroup", "Units"),

    ("LCIAMethod", "Name"),
    ("LCIAMethod", "Methodologies"),
    ("LCIAMethod", "Impact categories"),
    ("LCIAMethod", "Impact indicator"),
    ("LCIAMethod", "Description"),
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

    # try to find German translations
    kvmap = {}
    mpath = "../src/app/messages"
    with open(mpath + ".properties", "r") as f:
        for line in f:
            parts = line.split("=")
            if len(parts) < 2:
                continue
            kvmap[parts[0].strip().lower()] = parts[1].strip().lower()
    
    tmap = {}
    with open(mpath + "_de.properties", "r") as f:
        for line in f:
            parts = line.split("=")
            if len(parts) < 2:
                continue
            enval = kvmap.get(parts[0].strip().lower())
            if enval is None:
                continue
            tmap[enval] = parts[1].strip()

    print("\n\nMessages_de:\n\n")
    for pair in tooltips:
        k = make_key(pair)
        de_text = tmap.get(pair[1].strip().lower())
        if de_text is None:
            de_text = "(keine Beschreibung vorhanden)"
        print("%s = %s" % (k, de_text))
