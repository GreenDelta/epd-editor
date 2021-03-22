
import datetime
import os
import os.path
import shutil
import sys


def main():

    # delete the `dist` folder
    if os.path.exists('dist'):
        shutil.rmtree('dist')
    os.mkdir('dist')

    version = read_app_version()
    print(version)
    pack_win_app()


def pack_win_app():
    app_dir = 'win32.win32.x86_64/epd-editor'
    if not os.path.exists(app_dir):
        print('%s does not exist; skip Windows version' % app_dir)
        return

    print('build windows version')

    # copy the JRE into the app
    jre_dir = 'jre/win64'
    if not os.path.exists(jre_dir):
        sys.exit('ERROR: JRE not found in %s' % jre_dir)
    app_jre_dir = app_dir + "/jre"
    if not os.path.exists(app_jre_dir):
        print('  .. copy the JRE')
        shutil.copytree(jre_dir, app_jre_dir)

    # create the distribution package
    version = read_app_version()
    print('  .. create package')
    zip_file = 'dist/openLCA_win64_%s_%s' % (version, date())
    shutil.make_archive(zip_file, 'zip', 'win32.win32.x86_64')
    print('  done')


def read_app_version() -> str:
    """Read the version from the application manifest."""
    manifest = '../META-INF/MANIFEST.MF'
    with open(manifest, 'r', encoding='utf-8') as stream:
        for line in stream:
            if line.startswith('Bundle-Version'):
                return line.split(':')[1].strip()


def date() -> str:
    now = datetime.datetime.now()
    return '%d-%02d-%02d' % (now.year, now.month, now.day)


if __name__ == '__main__':
    main()
