@echo off

SET vdate=v2.0_2017_04_24

rmdir dist /s /q
mkdir dist

REM Copy the Java Runtime
robocopy jre\win64 win32.win32.x86_64\epd-editor\jre /e

REM Create the zip packages
cd win32.win32.x86_64
..\7za a ..\dist\epd-editor_%vdate%_win64.zip epd-editor
cd ..

cd macosx.cocoa.x86_64
..\7za a ..\dist\epd-editor_%vdate%_macosx.zip epd-editor
cd ..