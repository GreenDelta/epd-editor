# convert XML file with Q-Meta data questions to a JSON file

import xml.etree.cElementTree as dom
import json


def main():
    qs = []
    with open("qmeta_questions.xml", "r", encoding="utf-8") as f:
        root = dom.parse(f).getroot()
        for qnode in root.findall("Question"):
            qs.append({
                "id": qnode.findtext("QuestionID"),
                "group": qnode.findtext("QuestionGroup"),
                "answer": qnode.findtext("QuestionAnswerText"),
                "type":  qnode.findtext("QuestionAnswerType"),
            })

    with open("qmeta_questions.json", "w", encoding="utf-8") as f:
        json.dump(qs, f, indent="  ")


if __name__ == "__main__":
    main()
