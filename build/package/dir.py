from dataclasses import dataclass
from pathlib import Path
import shutil

from package import PROJECT_DIR
from package.dist import OsArch


@dataclass
class BuildDir:
    osa: OsArch

    @property
    def name(self):
        if self.osa == OsArch.LINUX_X64:
            return "linux.gtk.x86_64"
        if self.osa == OsArch.WINDOWS_X64:
            return "win32.win32.x86_64"
        if self.osa == OsArch.MACOS_X64:
            return "macosx.cocoa.x86_64"
        if self.osa == OsArch.MACOS_ARM:
            return "macosx.cocoa.aarch64"
        raise AssertionError(f"Unknown build name {self.osa}")

    @property
    def root(self) -> Path:
        return PROJECT_DIR / "build" / "temp" / self.name

    @property
    def export_dir(self) -> Path:
        return PROJECT_DIR / "build" / self.name

    @property
    def app(self) -> Path:
        if self.osa.is_mac():
            return self.root / "EPD Editor/EPD Editor.app/Contents/Eclipse"
        else:
            return self.root / "EPD Editor"

    @property
    def default_data(self) -> Path:
        return self.app / "data"

    @property
    def about(self) -> Path:
        return self.app

    @property
    def jre(self) -> Path:
        return self.app / "jre"

    def copy_export(self):
        if not self.export_dir.exists():
            print(f"No export available for copy the {self.osa.value} version.")
            return
        delete(self.root)
        self.root.parent.mkdir(exist_ok=True, parents=False)
        shutil.copytree(self.export_dir, self.root)

    def copy_default_data(self):
        default_data = PROJECT_DIR / "default_data"
        if not default_data.exists():
            return

        # copy the default data directory
        shutil.copytree(default_data, self.default_data, ignore=shutil.ignore_patterns(".gitignore"))

    @staticmethod
    def unjar_plugins(plugin_dir: Path):
        """Sometimes in newer Eclipse versions the PDE build does not respect
        the `Eclipse-BundleShape: dir` entry in plugin manifests anymore but
        exports them as jar-files. For plugins that should be extracted as
        folders, we check if there is a jar-file in the raw plugins folder
        and extract it if this is the case."""

        jars = [
            "org.eclipse.equinox.launcher.*.jar",
            "epd-editor_*.jar",
        ]
        for jar in jars:
            for g in plugin_dir.glob(jar):
                name = g.name[0:len(g.name) - 4]
                print(f"info: unpack plugin {name}")
                shutil.unpack_archive(g, plugin_dir / name, "zip")
                g.unlink()


def delete(path: Path):
    if not path.exists():
        return
    if path.is_dir():
        shutil.rmtree(path, ignore_errors=True)
    else:
        path.unlink(missing_ok=True)


class DistDir:
    @staticmethod
    def get() -> Path:
        d = PROJECT_DIR / "build/dist"
        if not d.exists():
            d.mkdir(parents=True, exist_ok=True)
        return d

    @staticmethod
    def clean():
        d = DistDir.get()
        if d.exists():
            print("Cleaning dist folder...")
            shutil.rmtree(d, ignore_errors=True)
        d.mkdir(parents=True, exist_ok=True)
