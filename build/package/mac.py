import shutil
import xml.etree.ElementTree as ElementTree

from pathlib import Path

from package import JRE_ID, PROJECT_DIR
from package.dir import BuildDir, delete
from package.dist import Version
from package.template import Template


class MacDir:
    @staticmethod
    def arrange(build_dir: BuildDir):

        # create the folder structure
        app_root = build_dir.root / "EPD Editor"
        bundle_dir = build_dir.root / "EPD Editor/EPD Editor.app"
        macos_dir = bundle_dir / "Contents/MacOS"
        for d in (bundle_dir, build_dir.app, macos_dir):
            d.mkdir(parents=True, exist_ok=True)

        # move files and folders
        moves = [
            (app_root / "configuration", build_dir.app),
            (app_root / "plugins", build_dir.app),
            (app_root / ".eclipseproduct", build_dir.app),
            (app_root / "Resources", bundle_dir / "Contents"),
            (app_root / "MacOS/EPD Editor", macos_dir / "EPD Editor"),
        ]
        for (source, target) in moves:
            if source.exists():
                shutil.move(str(source), str(target))

        MacDir.add_app_info(bundle_dir / "Contents/Info.plist")

        # create the ini file
        plugins_dir = build_dir.app / "plugins"
        launcher_jar = next(plugins_dir.glob("*launcher_*.jar")).name
        launcher_lib = next(plugins_dir.glob("*launcher.cocoa.macosx*")).name
        Template.apply(
            PROJECT_DIR / "templates/EPDEditor_mac.ini",
            build_dir.app / "EPD Editor.ini",
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
        version = Version.get().base
        info_dict = {
            "CFBundleShortVersionString": version,
            "CFBundleVersion": version,
        }
        MacDir.edit_plist(PROJECT_DIR / "templates/Info.plist", path, info_dict)

    @staticmethod
    def edit_jre_info(build_dir: BuildDir):
        path = build_dir.jre / "Contents/Info.plist"
        info_dict = {
            "CFBundleIdentifier": JRE_ID,
        }
        MacDir.edit_plist(path, path, info_dict)

    @staticmethod
    def edit_plist(path_in: Path, path_out: Path, info: dict[str, str]):
        plist = ElementTree.parse(path_in)
        element = plist.getroot().find("dict")
        if element is None:
            raise AttributeError("Warning: Could not parse the macOS plist.")
        iterator = element.iter()
        for elem in iterator:
            if elem.text is not None and elem.text in info.keys():
                string = next(iterator, None)
                if string is not None:
                    string.text = info[elem.text]

        with open(path_out, "wb") as out:
            out.write(
                b'<?xml version="1.0" encoding="UTF-8" standalone = '
                b'"no" ?>\n'
            )
            plist.write(out, encoding="UTF-8", xml_declaration=False)
