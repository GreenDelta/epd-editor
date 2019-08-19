#!/bin/bash
if [ $# -eq 0 ]; then
    echo
    echo "Usage: repair4mac [exportdir]"
    echo
    echo "       exportdir is the folder where the Eclipse application has been exported." 
    echo
    exit 1
fi

targetdir=$1
if [ ! -d "$targetdir" ]; then
   echo Folder $targetdir does not exist, aborting.
   exit 1
fi

mkdir -p $targetdir/epd-editor.app/Contents/MacOS
mv $targetdir/Info.plist $targetdir/epd-editor.app/Contents
mv $targetdir/Resources $targetdir/epd-editor.app/Contents
mv $targetdir/MacOS/epd-editor $targetdir/epd-editor.app/Contents/MacOS
rmdir $targetdir/MacOS
sed -i '' -e 's/Epd-editor/EPD-Editor/g' $targetdir/epd-editor.app/Contents/Info.plist
mv $targetdir/epd-editor.app $targetdir/EPD-Editor.app
