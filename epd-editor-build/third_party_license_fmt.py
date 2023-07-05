#
# This script generates the license information of the Maven dependencies
# for the about.html page. You would update this information everytime you
# update some dependency in the pom.xml configuration for a release. These
# are the steps:
#
# 1. Copy this script and the pom.xml in some folder (you do not have to
#    do this but this will keep generated things out of version control)
#
# 2. Collect the third party licenses with the Maven license plugin
#    (; you may have to change the build target from pom to jar in order
#    this to work): 
#
#    mvn license:add-third-party
#
#    This will generate a `THIRD-PARTY.txt` file somewhere deep in the
#    target folder.
#
# 3. Copy the generated file next to this script and execute the script.
#
# 4. Copy the script output into the about.html. 
#

def main():
    with open('THIRD-PARTY.txt', 'r', encoding='utf-8') as f:
        for line in f:
            s = line.strip()
            parts = []
            for p in s.split('('):
                for q in p.split(')'):
                    if q.strip() != '':
                        parts.append(q.strip())

            if len(parts) != 3:
                continue
            license: str = parts[0].strip()
            license_prefix = ''
            license_suffix = ''
            if not license.lower().startswith('the'):
                license_prefix = 'the '
            if 'license' not in license.lower():
                license_suffix = ' license'
            license = f'{license_prefix}<strong>{license}</strong>{license_suffix}'

            title = parts[1].strip()
            info = parts[2].split(' - ')
            if len(info) != 2:
                continue
            url = info[1].strip()
            
            print(f'\n<h2>{title}</h2>')
            print(f'<p>The <a href="{url}">{title}</a> library')
            print(f'is licensed under {license}.')
            print('See the project website for further information.</p>')


if __name__ == '__main__':
    main()