import argparse
import shutil

from package import PROJECT_DIR
from package.dir import DistDir, BuildDir, delete
from package.dist import OsArch, Version
from package.jre import JRE
from package.mac import MacDir
from package.template import Template
from package.zipio import Zip


def package(
    osa: OsArch,
    version: Version,
    build_dir: BuildDir,
):
    plugins_dir = (
        build_dir.root / "EPD Editor/plugins"
        if osa.is_mac()
        else build_dir.app / "plugins"
    )
    BuildDir.unjar_plugins(plugins_dir)

    if osa.is_mac():
        MacDir.arrange(build_dir)

    # JRE and native libraries
    JRE.extract_to(build_dir)

    # edit the JRE Info.plist
    if osa.is_mac():
        MacDir.edit_jre_info(build_dir)

    # copy ini
    if osa.is_win():
        Template.apply(
            PROJECT_DIR / "templates/EPDEditor_win.ini",
            build_dir.app / "EPD Editor.ini",
            encoding="iso-8859-1",
            lang="en",
        )
    if osa.is_linux():
        shutil.copy2(
            PROJECT_DIR / "templates/EPDEditor_linux.ini",
            build_dir.app / "EPD Editor.ini",
        )

    # copy default data
    print("  copy default data")
    build_dir.copy_default_data()

    # build the package
    pack_name = f"EPDEditor_{osa.value}_{version.app_suffix}"

    print(f"  Creating package {pack_name}...")
    pack = DistDir.get() / pack_name
    if osa == OsArch.WINDOWS_X64:
        shutil.make_archive(pack.as_posix(), "zip", build_dir.root.as_posix())
    else:
        Zip.targz(build_dir.root, pack)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "-c", "--clean", help="delete the last build files", action="store_true"
    )
    args = parser.parse_args()

    # delete build resources
    DistDir.clean()
    if args.clean:
        for arch in OsArch:
            build_dir = BuildDir(arch)
            if build_dir.root.exists():
                print(f"delete: ${build_dir.root}")
                shutil.rmtree(build_dir.root)
            if build_dir.export_dir.exists():
                print(f"delete: ${build_dir.export_dir}")
                shutil.rmtree(build_dir.export_dir)
        return

    version = Version.get()
    for osa in OsArch:
        build_dir = BuildDir(osa)

        if not build_dir.export_dir.exists():
            print(f"No {osa} export is available; skipped")
            continue

        print(f"\nCopying the {osa.value} export...")
        build_dir.copy_export()

        print(f"Packaging the {osa.value} build...")
        package(osa, version, build_dir)
        delete(build_dir.root)
        print(f"Done packaging the {osa.value} build.")


if __name__ == "__main__":
    main()
