@echo off

if exist dist (
    echo delete folder dist
    rmdir dist /s /q
)

if exist win32.win32.x86_64 (
    echo delete folder win32.win32.x86_64
    rmdir win32.win32.x86_64 /s /q
)

if exist macosx.cocoa.x86_64 (
    echo delete folder macosx.cocoa.x86_64
    rmdir macosx.cocoa.x86_64 /s /q
)
