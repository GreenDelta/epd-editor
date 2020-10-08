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
mkdir -p $targetdir/epd-editor.app/Contents/Eclipse
mv $targetdir/Info.plist $targetdir/epd-editor.app/Contents
mv $targetdir/Resources $targetdir/epd-editor.app/Contents
mv $targetdir/MacOS/epd-editor $targetdir/epd-editor.app/Contents/MacOS
#mv $targetdir/configuration $targetdir/epd-editor.app/Contents/Eclipse
#mv $targetdir/plugins $targetdir/epd-editor.app/Contents/Eclipse

mv $targetdir/jre $targetdir/epd-editor.app
rmdir $targetdir/MacOS
sed -i '' -e 's/Epd-editor/EPD-Editor/g' $targetdir/epd-editor.app/Contents/Info.plist
sed -i '' -e 's/\<array\>/\<array\>\<string\>-vm\<\/string\>\<string\>.\.\/.\.\/jre\/bin\/java\<\/string\>/g' $targetdir/epd-editor.app/Contents/Info.plist

#echo -e "-startup\n../Eclipse/plugins/org.eclipse.equinox.launcher_1.5.700.v20200207-2156.jar" >> $targetdir/epd-editor.app/Contents/MacOS/epd-editor.ini
#echo -e "--launcher.library\n../Eclipse/plugins/org.eclipse.equinox.launcher.cocoa.macosx.x86_64_1.1.1100.v20190907-0426" >> $targetdir/epd-editor.app/Contents/MacOS/epd-editor.ini
echo -e "-vm\n../../jre/bin/java" >> $targetdir/epd-editor.app/Contents/MacOS/epd-editor.ini

mv $targetdir/epd-editor.app $targetdir/EPD-Editor.app
