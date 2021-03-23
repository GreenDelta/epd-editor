
import datetime
import glob
import os
import shutil
import subprocess
import sys

from os.path import exists


def main():

    # delete the `dist` folder
    if exists('dist'):
        print('clear `dist` folder')
        shutil.rmtree('dist')
    os.mkdir('dist')

    version = read_app_version()
    print(version)
    # pack_win_app()
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
        shutil.copytree(jre_dir, app_jre_dir)

    # create the distribution package
    version = read_app_version()
    print('  .. create package')
    zip_file = 'dist/epd-editor_win64_%s_%s' % (version, date())
    shutil.make_archive(zip_file, 'zip', 'win32.win32.x86_64')
    print('  done')


def pack_mac_app():
    base_dir = 'macosx.cocoa.x86_64/epd-editor'
    app_dir = 'macosx.cocoa.x86_64/epd-editor/epd-editor.app'
    if not exists(base_dir):
        print('%s does not exist; skip macOS version' % base_dir)
        return

    print('build macOS version')

    # copy the JRE version
    jre_tar = glob.glob('jre/*mac*.tar')
    if len(jre_tar) == 0:
        sys.exit('ERROR: could not find JRE for macOS')
    if not exists(app_dir + '/jre'):
        print('  .. copy the JRE')
        unzip(jre_tar[0], app_dir)
        # shutil.unpack_archive(jre_tar[0], app_dir + '/epd-editor.app')
        jre_dir = glob.glob(app_dir + '/*jre*')
        for _dir in jre_dir:
            if os.path.isdir(_dir):
                os.rename(_dir, app_dir + '/jre')

    # move things around
    print('  .. restructure the app folder')
    shutil.copyfile('macos/Info.plist', app_dir + '/Contents/Info.plist')
    eclipse_contents = [
        '/configuration',
        '/plugins',
        '/.eclipseproduct',
    ]
    for f in eclipse_contents:
        if exists(base_dir + f):
            shutil.move(base_dir + f, app_dir + '/Contents/Eclipse')

    if exists(base_dir + '/Resources'):
        shutil.move(base_dir + '/Resources', app_dir + '/Contents')
    if exists(base_dir+'/MacOS/epd-editor'):
        shutil.copy2(base_dir+'/MacOS/epd-editor',
                     app_dir + '/Contents/MacOS/eclipse')

    # create the ini-file
    plugins_dir = app_dir + '/Contents/Eclipse/plugins/'
    launcher_jar = os.path.basename(
        glob.glob(plugins_dir + '*launcher*.jar')[0])
    launcher_lib = os.path.basename(
        glob.glob(plugins_dir + '*launcher.cocoa.macosx*')[0])
    with open("macos/eclipse.ini", mode='r', encoding="utf-8") as f:
        text = f.read()
        text = text.format(launcher_jar=launcher_jar,
                           launcher_lib=launcher_lib)
        out_ini_path = app_dir + "/Contents/Eclipse/eclipse.ini"
        with open(out_ini_path, mode='w', encoding='utf-8', newline='\n') as o:
            o.write(text)

    shutil.rmtree(base_dir + "/MacOS")
    os.remove(base_dir + "/Info.plist")
    os.remove(app_dir + "/Contents/MacOS/epd-editor.ini")

    version = read_app_version()
    print('  .. create package')
    dist_file = 'dist/epd-editor_macOS_%s_%s' % (version, date())
    # shutil.make_archive(dist_file, 'gztar', 'macosx.cocoa.x86_64/epd-editor')
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


def targz(folder, tar_file):
    if not exists('7za.exe'):
        shutil.make_archive(tar_file, 'gztar', folder)
        return
    cmd = ['7za.exe', 'a', '-ttar', tar_file + '.tar', folder + '/*']
    subprocess.call(cmd)
    cmd = ['7za.exe', 'a', '-tgzip', tar_file + '.tar.gz', tar_file + '.tar']
    subprocess.call(cmd)
    os.remove(tar_file + '.tar')


def unzip(zip_file, to_dir):
    if not os.path.exists(to_dir):
        os.makedirs(to_dir)
    if not exists('7za.exe'):
        shutil.unpack_archive(zip_file, to_dir)
        return
    cmd = ['7za.exe', 'x', zip_file, '-o%s' % to_dir]
    subprocess.call(cmd)


if __name__ == '__main__':
    main()
