import os


def main():
    dir_path = os.path.dirname(os.path.realpath(__file__))
    messages_path = dir_path + '/../src/app/messages.properties'
    letter = 'a'
    for key in get_prop_keys(messages_path):
        if key[0].lower() != letter:
            print()
            letter = key[0].lower()
        print('\tpublic static String %s;' % key)


def get_prop_keys(messages_path):
    keys = []
    with open(messages_path, 'r', encoding='iso-8859-1', newline='\n') as f:
        for line in f:
            l = line.strip()
            if l.startswith('#') or ('=' not in line):
                continue
            key = l.split('=')[0].strip()
            keys.append(key)
    keys.sort()
    return keys

if __name__ == '__main__':
    main()