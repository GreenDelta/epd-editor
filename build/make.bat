@echo off

SET version=2.0

REM see https://stackoverflow.com/questions/19131029/how-to-get-date-in-bat-file
FOR /f "tokens=2 delims==" %%a IN ('wmic OS Get localdatetime /value') DO SET "dt=%%a"
set "YYYY=%dt:~0,4%"
set "MM=%dt:~4,2%"
set "DD=%dt:~6,2%"

SET vdate="%version%_%YYYY%_%MM%_%DD%"

if exist dist rmdir dist /s /q
mkdir dist

IF EXIST win32.win32.x86_64 (
	REM Build Windows 64-bit version
	robocopy jre\win64 win32.win32.x86_64\epd-editor\jre /e
	robocopy default_data win32.win32.x86_64\epd-editor\data /e
	cd win32.win32.x86_64
	..\7za a ..\dist\epd-editor_%vdate%_win64.zip epd-editor
	cd ..
)

IF EXIST win32.win32.x86 (
	REM Build Windows 32-bit version
	robocopy jre\win32 win32.win32.x86\epd-editor\jre /e
	robocopy default_data win32.win32.x86\epd-editor\data /e
	cd win32.win32.x86
	..\7za a ..\dist\epd-editor_%vdate%_win32.zip epd-editor
	cd ..
)


REM cd macosx.cocoa.x86_64
REM ..\7za a ..\dist\epd-editor_%vdate%_macosx.zip epd-editor
REM cd ..

:DONE
