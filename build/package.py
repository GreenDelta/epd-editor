import datetime
import os
import platform
import re
import shutil
import subprocess
import urllib.request
import xml.etree.ElementTree as ElementTree

from enum import Enum
from dataclasses import dataclass
from pathlib import Path
from typing import Optional

# the bundle ID of the JRE
JRE_ID = "org.epd.editor.jre"

# the root of the build project epd-editor/epd-editor-build
PROJECT_DIR = Path(os.path.dirname(os.path.abspath(__file__)))


class OsArch(Enum):
    MACOS_ARM = "macOS_arm64"
    MACOS_X64 = "macOS_x64"
    WINDOWS_X64 = "Windows_x64"
    LINUX_X64 = "Linux_x64"

    def is_mac(self) -> bool:
        return self == OsArch.MACOS_ARM or self == OsArch.MACOS_X64

    def is_win(self) -> bool:
        return self == OsArch.WINDOWS_X64

    def is_linux(self) -> bool:
        return self == OsArch.LINUX_X64


@dataclass
class Version:
    app_version: str

    @staticmethod
    def get() -> "Version":
        # read app version from the app-manifest
        manifest = PROJECT_DIR.parent / Path("META-INF/MANIFEST.MF")
        print(f"read version from {manifest}")
        app_version = None
        with open(manifest, "r", encoding="utf-8") as f:
            for line in f:
                text = line.strip()
                if not text.startswith("Bundle-Version"):
                    continue
                app_version = text.split(":")[1].strip()
                break
        if app_version is None:
            app_version = "5.0.0"
            print(
                f"WARNING failed to read version from {manifest},"
                f" default to {app_version}"
            )
        return Version(app_version)

    @property
    def app_suffix(self):
        return f"{self.app_version}_{datetime.date.today().isoformat()}"

    @property
    def base(self) -> str:
        m = re.search(r"(\d+(\.\d+)?(\.\d+)?)", self.app_version)
        return "2" if m is None else m.group(0)


class Zip:
    __zip: Optional["Zip"] = None

    def __init__(self, is_z7: bool):
        self.is_z7 = is_z7

    @staticmethod
    def z7() -> Path:
        return PROJECT_DIR / "tools/7zip/7za.exe"

    @staticmethod
    def get() -> "Zip":
        if Zip.__zip is not None:
            return Zip.__zip
        system = platform.system().lower()
        if system != "windows":
            Zip.__zip = Zip(False)
            return Zip.__zip
        z7 = Zip.z7()
        if os.path.exists(z7):
            Zip.__zip = Zip(True)
            return Zip.__zip

        # try to fetch a version 7zip version from the web
        url = "https://www.7-zip.org/a/7za920.zip"
        print(
            f"WARNING no 7zip version found under {z7}, will download an OLD"
            f" version from {url}"
        )
        z7_dir = PROJECT_DIR / "tools/7zip"
        z7_dir.mkdir(parents=True, exist_ok=True)
        z7_zip = z7_dir / "7zip.zip"
        urllib.request.urlretrieve(url, z7_zip)
        shutil.unpack_archive(z7_zip, z7_dir)
        Zip.__zip = Zip(os.path.exists(z7))
        return Zip.__zip

    @staticmethod
    def unzip(zip_file: Path, target_folder: Path):
        """Extracts the content of the given zip file under the given path."""
        if not target_folder.exists():
            target_folder.mkdir(parents=True, exist_ok=True)
        if Zip.get().is_z7:
            subprocess.call([Zip.z7(), "x", zip_file, f"-o{target_folder}"])
        else:
            shutil.unpack_archive(zip_file, target_folder)

    @staticmethod
    def targz(folder: Path, target: Path):
        if not target.parent.exists():
            target.parent.mkdir(parents=True, exist_ok=True)

        # remove possible extensions from the given target file
        base_name = target.name
        if base_name.endswith(".tar.gz"):
            base_name = base_name[0:-7]
        elif base_name.endswith(".tar"):
            base_name = base_name[0:-4]
        base = target.parent / base_name

        # package the folder
        if Zip.get().is_z7:
            tar = target.parent / (base_name + ".tar")
            gz = target.parent / (base_name + ".tar.gz")
            subprocess.call(
                [Zip.z7(), "a", "-ttar", str(tar), folder.as_posix() + "/*"]
            )
            subprocess.call([Zip.z7(), "a", "-tgzip", str(gz), str(tar)])
            os.remove(tar)
        else:
            shutil.make_archive(str(base), "gztar", str(folder))


class Build:
    @staticmethod
    def dist_dir() -> Path:
        d = PROJECT_DIR / "build/dist"
        if not d.exists():
            d.mkdir(parents=True, exist_ok=True)
        return d

    @staticmethod
    def clean():
        d = Build.dist_dir()
        if d.exists():
            print("clean dist folder")
            shutil.rmtree(d, ignore_errors=True)
        d.mkdir(parents=True, exist_ok=True)


@dataclass
class BuildDir:
    osa: OsArch

    @property
    def root(self) -> Path:
        build_dir = PROJECT_DIR / "build"
        if self.osa == OsArch.LINUX_X64:
            return build_dir / "linux.gtk.x86_64"
        if self.osa == OsArch.WINDOWS_X64:
            return build_dir / "win32.win32.x86_64"
        if self.osa == OsArch.MACOS_X64:
            return build_dir / "macosx.cocoa.x86_64"
        if self.osa == OsArch.MACOS_ARM:
            return build_dir / "macosx.cocoa.aarch64"
        raise AssertionError(f"unknown build target {self.osa}")

    @property
    def exists(self) -> bool:
        return self.root.exists()

    @property
    def app_dir(self) -> Path:
        if self.osa.is_mac():
            return self.root / "EPD Editor/EPD Editor.app"
        else:
            return self.root / "EPD Editor"

    @property
    def about_dir(self) -> Path:
        if self.osa.is_mac():
            return self.app_dir / "Contents/Eclipse"
        else:
            return self.app_dir

    @property
    def jre_dir(self) -> Path:
        if self.osa.is_mac():
            return self.app_dir / "Contents/Eclipse/jre"
        else:
            return self.app_dir / "jre"

    @property
    def default_data_dir(self) -> Path:
        if self.osa.is_mac():
            return self.app_dir / "Contents/Eclipse/data"
        else:
            return self.app_dir / "data"

    @property
    def olca_plugin_dir(self) -> Path | None:
        if self.osa.is_mac():
            plugin_dir = self.app_dir / "Contents/Eclipse/plugins"
        else:
            plugin_dir = self.app_dir / "plugins"
        if not plugin_dir.exists() or not plugin_dir.is_dir():
            print(f"warning: could not locate plugin folder: {plugin_dir}")
            return None
        for p in plugin_dir.iterdir():
            if p.name.startswith("olca-app") and p.is_dir():
                return p
        print(f"warning: olca-app plugin folder not found in: {plugin_dir}")
        return None

    def package(self, version: Version):
        if self.osa.is_mac():
            MacDir.arrange(self)

        JRE.extract_to(self)

        # edit the JRE Info.plist
        if self.osa.is_mac():
            MacDir.edit_jre_info(self)

        # copy credits
        print("  copy credits")
        about_page = PROJECT_DIR / "credits/about.html"
        if about_page.exists():
            shutil.copy2(about_page, self.about_dir)
            plugin_dir = self.olca_plugin_dir
            if plugin_dir:
                shutil.copy2(about_page, plugin_dir)

        # copy default data
        print("  copy default data")
        default_data = PROJECT_DIR / "default_data"
        if default_data.exists():
            shutil.copytree(default_data, self.default_data_dir)

        # copy ini and bin files
        if self.osa.is_win():
            Template.apply(
                PROJECT_DIR / "templates/EPDEditor_win.ini",
                self.app_dir / "EPD Editor.ini",
                encoding="iso-8859-1",
                lang="en",
            )
        if self.osa.is_linux():
            shutil.copy2(
                PROJECT_DIR / "templates/EPDEditor_linux.ini",
                self.app_dir / "EPD Editor.ini",
            )
        # build the package
        pack_name = f"EPDEditor_{self.osa.value}_{version.app_suffix}"
        print(f"  create package {pack_name}")
        pack = Build.dist_dir() / pack_name
        if self.osa == OsArch.WINDOWS_X64:
            shutil.make_archive(pack.as_posix(), "zip", self.root.as_posix())
        else:
            Zip.targz(self.root, pack)


class JRE:
    @staticmethod
    def zip_name(osa: OsArch) -> str:
        suffix = "zip" if osa == OsArch.WINDOWS_X64 else "tar.gz"
        if osa == OsArch.MACOS_ARM:
            name = "aarch64_mac"
        elif osa == OsArch.MACOS_X64:
            name = "x64_mac"
        elif osa == OsArch.LINUX_X64:
            name = "x64_linux"
        elif osa == OsArch.WINDOWS_X64:
            name = "x64_windows"
        else:
            raise ValueError(f"unsupported OS+arch: {osa}")
        return f"OpenJDK17U-jre_{name}_hotspot_17.0.5_8.{suffix}"

    @staticmethod
    def cache_dir() -> Path:
        d = PROJECT_DIR / "runtime/jre"
        if not os.path.exists(d):
            d.mkdir(parents=True, exist_ok=True)
        return d

    @staticmethod
    def fetch(osa: OsArch) -> Path:
        zip_name = JRE.zip_name(osa)
        cache_dir = JRE.cache_dir()
        zf = cache_dir / JRE.zip_name(osa)
        if os.path.exists(zf):
            return zf
        url = (
            "https://github.com/adoptium/temurin17-binaries/releases/"
            f"download/jdk-17.0.5%2B8/{zip_name}"
        )
        print(f"  download JRE from {url}")
        urllib.request.urlretrieve(url, zf)
        if not os.path.exists(zf):
            raise AssertionError(f"JRE download failed; url={url}")
        return zf

    @staticmethod
    def extract_to(build_dir: BuildDir):
        if build_dir.jre_dir.exists():
            return
        print("  copy JRE")

        # fetch and extract the JRE
        zf = JRE.fetch(build_dir.osa)

        ziptool = Zip.get()
        if not ziptool.is_z7 or zf.name.endswith(".zip"):
            Zip.unzip(zf, build_dir.app_dir)
        else:
            tar = zf.parent / zf.name[0:-3]
            if not tar.exists():
                Zip.unzip(zf, zf.parent)
                if not tar.exists():
                    raise AssertionError(f"could not find JRE tar {tar}")
            Zip.unzip(tar, build_dir.app_dir)

        # rename the JRE folder if required
        if build_dir.jre_dir.exists():
            return
        jre_dir = next(build_dir.app_dir.glob("*jre*"))
        os.rename(jre_dir, build_dir.jre_dir)

        # delete a possible client VM (the server VM is much faster)
        client_dir = build_dir.jre_dir / "bin/client"
        if client_dir.exists():
            delete(client_dir)


class MacDir:
    @staticmethod
    def arrange(build_dir: BuildDir):
        # create the folder structure
        app_root = build_dir.root / "EPD Editor"
        app_dir = build_dir.app_dir
        eclipse_dir = app_dir / "Contents/Eclipse"
        macos_dir = app_dir / "Contents/MacOS"
        for d in (app_dir, eclipse_dir, macos_dir):
            d.mkdir(parents=True, exist_ok=True)

        # move files and folders
        moves = [
            (app_root / "configuration", eclipse_dir),
            (app_root / "plugins", eclipse_dir),
            (app_root / ".eclipseproduct", eclipse_dir),
            (app_root / "Resources", app_dir / "Contents"),
            (app_root / "MacOS/EPD Editor", macos_dir / "EPD Editor"),
        ]
        for (source, target) in moves:
            if source.exists():
                shutil.move(str(source), str(target))

        MacDir.add_app_info(app_dir / "Contents/Info.plist")

        # create the ini file
        plugins_dir = eclipse_dir / "plugins"
        launcher_jar = next(plugins_dir.glob("*launcher*.jar")).name
        launcher_lib = next(plugins_dir.glob("*launcher.cocoa.macosx*")).name
        Template.apply(
            PROJECT_DIR / "templates/EPDEditor_mac.ini",
            eclipse_dir / "EPD Editor.ini",
            launcher_jar=launcher_jar,
            launcher_lib=launcher_lib,
        )

        # clean up
        delete(app_root / "MacOS")
        delete(app_root / "Info.plist")
        delete(macos_dir / "EPD Editor.ini")

    @staticmethod
    def add_app_info(path: Path):
        # set version of the app
        # (version must be composed of one to three period-separated integers.)
        info_dict = {
            "CFBundleShortVersionString": Version.get().base,
            "CFBundleVersion": Version.get().base,
        }
        MacDir.edit_plist(PROJECT_DIR / "templates/Info.plist", path, info_dict)

    @staticmethod
    def edit_jre_info(build_dir: BuildDir):
        path = build_dir.jre_dir / "Contents/Info.plist"
        info_dict = {
            "CFBundleIdentifier": JRE_ID,
        }
        MacDir.edit_plist(path, path, info_dict)

    @staticmethod
    def edit_plist(path_in: Path, path_out: Path, info: dict):
        plist = ElementTree.parse(path_in)
        iterator = plist.getroot().find("dict").iter()
        for elem in iterator:
            if elem.text in info.keys():
                string = next(iterator, None)
                if string is not None:
                    string.text = info[elem.text]

        with open(path_out, "wb") as out:
            out.write(
                b'<?xml version="1.0" encoding="UTF-8" standalone = '
                b'"no" ?>\n'
            )
            plist.write(out, encoding="UTF-8", xml_declaration=False)


class Template:
    @staticmethod
    def apply(
        source: Path, target: Path, encoding: str = "utf-8", **kwargs: str
    ):
        with open(source, mode="r", encoding="utf-8") as inp:
            template = inp.read()
            text = template.format(**kwargs)
        with open(target, "w", encoding=encoding) as out:
            out.write(text)


def main():
    Build.clean()
    version = Version.get()
    for osa in OsArch:
        build_dir = BuildDir(osa)
        if not build_dir.exists:
            print(f"no {osa} build available; skipped")
            continue
        print(f"package build: {osa}")
        build_dir.package(version)


def delete(path: Path):
    if path is None or not path.exists():
        return
    if path.is_dir():
        shutil.rmtree(path, ignore_errors=True)
    else:
        path.unlink(missing_ok=True)


if __name__ == "__main__":
    main()
