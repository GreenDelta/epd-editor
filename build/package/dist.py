import argparse
import datetime
import os
import re

from dataclasses import dataclass
from enum import Enum
from pathlib import Path
import sys

from package import PROJECT_DIR


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
        print(f"Reading version from {manifest}...")
        app_version = None
        with open(manifest, "r", encoding="utf-8") as f:
            for line in f:
                text = line.strip()
                if not text.startswith("Bundle-Version"):
                    continue
                app_version = text.split(":")[1].strip()
                break
        if app_version is None:
            app_version = "6.0.0"
            print(
                f"Warning: failed to read version from {manifest},"
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


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "-v",
        "--version",
        help="get the version of the EPD Editor",
        action="store_true",
    )

    args = parser.parse_args()
    if args.version:
        # Silencing the prints in the script
        sys.stdout = open(os.devnull, 'w')
        suffix = Version.get().app_suffix
        # Restoring the prints
        sys.stdout = sys.__stdout__
        print(suffix)


if __name__ == "__main__":
    main()
