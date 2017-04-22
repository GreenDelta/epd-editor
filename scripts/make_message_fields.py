import os

HEAD = '''package app;

import org.eclipse.osgi.util.NLS;

public class M extends NLS {

'''

TAIL = '''
\tstatic {
\t\tNLS.initializeMessages("app.messages", M.class);
\t}

\tprivate M() {
\t}
}
'''


def main():
    dir_path = os.path.dirname(os.path.realpath(__file__))
    messages_path = dir_path + '/../src/app/messages.properties'
    java_path = dir_path + '/../src/app/M.java'
    letter = 'a'
    java = HEAD
    for key in get_prop_keys(messages_path):
        if key[0].lower() != letter:
            java = java + '\n'
            letter = key[0].lower()
        java = java + ('\tpublic static String %s;\n' % key)
    java = java + TAIL
    with open(java_path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(java)


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