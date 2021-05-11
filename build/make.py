import datetime
import os
import subprocess
import sys

from glob import glob
from os.path import basename, exists
from shutil import copyfile, copytree, make_archive, move, rmtree,\
    unpack_archive


def main():

    # delete the `dist` folder
    if exists('dist'):
        print('clear `dist` folder')
        rmtree('dist')
    os.mkdir('dist')

    version = read_app_version()
    print(version)
    pack_win_app()
    pack_mac_app()


def pack_win_app():
    app_dir = 'win32.win32.x86_64/epd-editor'
    if not exists(app_dir):
        print('%s does not exist; skip Windows version' % app_dir)
        return

    print('build windows version')

    # copy the JRE into the app
    jre_dir = 'jre/win64'
    if not exists(jre_dir):
        sys.exit('ERROR: JRE not found in %s' % jre_dir)
    app_jre_dir = app_dir + '/jre'
    if not exists(app_jre_dir):
        print('  .. copy the JRE')
        copytree(jre_dir, app_jre_dir)

    # copy the default data into the app
    if not exists(app_dir + '/data'):
        copytree('default_data', app_dir + '/data')

    # create the distribution package
    version = read_app_version()
    print('  .. create package')
    zip_file = 'dist/epd-editor_win64_%s_%s' % (version, date())
    make_archive(zip_file, 'zip', 'win32.win32.x86_64')
    print('  done')


def pack_mac_app():
    base_dir = 'macosx.cocoa.x86_64/epd-editor'
    app_dir = 'macosx.cocoa.x86_64/epd-editor/epd-editor.app'
    if not exists(base_dir):
        print('%s does not exist; skip macOS version' % base_dir)
        return

    print('build macOS version')

    # move things around
    print('  .. restructure the app folder')
    os.makedirs(app_dir + '/Contents/Eclipse', exist_ok=True)
    os.makedirs(app_dir + '/Contents/MacOS', exist_ok=True)
    copyfile('macos/Info.plist', app_dir + '/Contents/Info.plist')
    move(base_dir + '/configuration', app_dir + '/Contents/Eclipse')
    move(base_dir + '/plugins', app_dir + '/Contents/Eclipse')
    move(base_dir + '/.eclipseproduct', app_dir + '/Contents/Eclipse')
    move(base_dir + '/Resources', app_dir + '/Contents')
    copyfile(base_dir+'/MacOS/epd-editor', app_dir + '/Contents/MacOS/eclipse')
    copytree('default_data', app_dir + '/Contents/MacOS/data')
    rmtree(base_dir + "/MacOS")
    os.remove(base_dir + "/Info.plist")
    os.remove(app_dir + "/Contents/MacOS/epd-editor.ini")

    # create the ini-file
    plugins_dir = app_dir + '/Contents/Eclipse/plugins/'
    launcher_jar = basename(glob(plugins_dir + '*launcher*.jar')[0])
    launcher_lib = basename(glob(plugins_dir + '*launcher.cocoa.macosx*')[0])
    with open("macos/eclipse.ini", mode='r', encoding="utf-8") as f:
        text = f.read()
        text = text.format(launcher_jar=launcher_jar,
                           launcher_lib=launcher_lib)
        out_ini_path = app_dir + "/Contents/Eclipse/eclipse.ini"
        with open(out_ini_path, mode='w', encoding='utf-8', newline='\n') as o:
            o.write(text)

    # copy the JRE version
    jre_tar = glob('jre/*mac*.tar')
    if len(jre_tar) == 0:
        sys.exit('ERROR: could not find JRE for macOS')
    print('  .. copy the JRE')
    unzip(jre_tar[0], app_dir)
    jre_dir = glob(app_dir + '/*jre*')[0]
    os.rename(jre_dir, app_dir + '/jre')

    version = read_app_version()
    print('  .. create package')
    dist_file = 'dist/epd-editor_macOS_%s_%s' % (version, date())
    targz('macosx.cocoa.x86_64/epd-editor', dist_file)
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


def targz(folder: str, out_name: str):
    """Creates a tar.gz file of the given folder. Uses 7zip if the executable
       is available in the build folder. The given out_name should be the path
       to the output file without the tar.gz extension."""
    if not exists('7za.exe'):
        make_archive(out_name, 'gztar', folder)
        return
    folder_ = './' + folder + '/*'
    cmd = ['7za.exe', 'a', '-ttar', out_name + '.tar', folder_]
    subprocess.call(cmd)
    cmd = ['7za.exe', 'a', '-tgzip', out_name + '.tar.gz', out_name + '.tar']
    subprocess.call(cmd)
    os.remove(out_name + '.tar')


def unzip(zip_file, to_dir):
    if not os.path.exists(to_dir):
        os.makedirs(to_dir)
    if not exists('7za.exe'):
        unpack_archive(zip_file, to_dir)
        return
    cmd = ['7za.exe', 'x', zip_file, '-o%s' % to_dir]
    subprocess.call(cmd)


if __name__ == '__main__':
    main()
