# EPD Editor
This is an editor for [ILCD](http://eplca.jrc.ec.europa.eu/LCDN/developer.xhtml)
data sets with [EPD format extensions](http://www.oekobaudat.de/en/info/working-group-indata.html). 

## Building from source
The EPD editor is an [Eclipse RCP](https://wiki.eclipse.org/Rich_Client_Platform)
application. To compile it from source you need to have the following tools
installed:

* a [Java Development Kit v8](https://adoptopenjdk.net/)
* [Maven](http://maven.apache.org/)
* the [Eclipse package for RCP developers](https://www.eclipse.org/downloads/)

When you have these tools installed you can build the application from source
via the following steps:

#### Install the openLCA core modules and ILCD validation API
The EPD Editor uses the current version of the 
[openLCA core modules](https://github.com/GreenDelta/olca-modules) for reading
and writing ILCD data sets and the [ILCD validation API](https://bitbucket.org/okusche/ilcdvalidation)
for validating them. These modules are plain Maven projects and can be
installed via `mvn install`. See the documentation of both projects for a
more information.

#### Get the source code of the application
We recommend to use Git to manage the source code but you can also download the
source code as a [zip file](https://github.com/GreenDelta/epd-editor/archive/master.zip).
If you have Git installed, just clone the repository via:

    git clone https://github.com/GreenDelta/epd-editor.git

The project folder should look like this:

    epd-editor
      .git/
      src/
      icons/
      META_INF/
      ...
      pom.xml
      ...

#### Copy the Maven dependencies
We use Maven to manage our non-Eclipse library dependencies. To pull them into
the project, just execute `mvn package` in the project folder:

```bash
cd epd-editor
mvn package
```

This will copy these libraries under the `epd-editor/libs` folder. 

#### Set up the Eclipse project
Open Eclipse and select/create a workspace directory. Import the `epd-editor` 
project into Eclipse via `Import/General/Existing Projects into Workspace`
(select the `epd-editor` folder). Open the file `platform.target` and click on
'Set as target platform' on the top right of the editor. This will download the
runtime platform into the folder `.metadata/.plugins/org.eclipse.pde.core/.bundle_pool`
of your workspace and thus may take a bit of time. After this, the project should
have no compile errors and you should be able to open the `app.product` file
and launch the application (click on `Launch an Eclipse application`).

The target platform is configured for multi-platform builds as described
[here](https://stackoverflow.com/a/12737382). You may have to update the target
platform when setting up the development environment.

Also, when updating the target platform it is probably required to update the product
configuration in `app.product`. Just remove and re-add all required plugins. It is
important to set the start levels for the following plugings to these values:

```xml
<configurations>
  <plugin id="org.eclipse.core.runtime" autoStart="true" startLevel="0" />
  <plugin id="org.eclipse.equinox.common" autoStart="true" startLevel="2" />
  <plugin id="org.eclipse.equinox.ds" autoStart="true" startLevel="2" />
  <plugin id="org.eclipse.equinox.event" autoStart="true" startLevel="2" />
  <plugin id="org.eclipse.equinox.simpleconfigurator" autoStart="true" startLevel="1" />
</configurations>
```

#### Labels and translations
Labels and translations are externalized in the `src/app/messages*.properties`
files. The keys in these files map to a static field in the class `app.M` which
are then used in the Java code. It is recommended to use 
[JLokalize](http://jlokalize.sourceforge.net) to edit the `messages*.properties`
files. Labels that are not externalized yet start with a hash mark `#`. Thus,
searching the Java source code for `"#` should give a list of strings that need
to be externalized (if there are any). The script `scripts/make_messages_fields.py`
generates the list of fields for the class `app.M` from the `messages.properties`
file.

#### Validation profile
The EPD-Editor uses the EPD profile from the 
[ILCD Validation API](https://bitbucket.org/okusche/ilcdvalidation). This
profile needs to be located under `validation_profile/EPD_validation_profile.jar`
and is not added to this repository. Thus, you need to copy the EPD profile
from the validation API to this location before testing the validation feature
or running a build.

#### Building the distribution packages
We currently build the application via the Eclipse export: right-click on the
project and select `Export > Eclipse product`. In the wizard select the
following settings:

* Configuration: `epd-editor/app-product` (should be the default)
* Root directory: `epd-editor`
* Synchronize before exporting: yes [x]
* Destination directory: choose the `build` folder of this project
* Generate p2 repository: no [ ] (would be just overhead)
* Export for multiple platforms: yes [x]
* (take the defaults for the others)

In the next page, select the platforms for which you want to build the product.
However, only the Windows `x86_64` version is currently fully supported when
building the distribution package and also the build is written for Windows.

Unfortunately, with Java 10 the export currently produces the following error in
Exlipse Oxygen and Photon:

>  option -bootclasspath not supported at compliance level 9 and above

This is related to [this bug in Eclipse](https://bugs.eclipse.org/bugs/show_bug.cgi?id=525280).
As also a Maven Tycho build currently fails with Java 10 due to 
[this bug](https://bugs.eclipse.org/bugs/show_bug.cgi?id=525522) (which is marked as fixed
but still appears in our EPD-Editor build), we need to set the Java compliance level
to Java 8 in Eclipse and the project configuration in order to have a working build.

##### Windows 

The `make.bat` script packages then the application (and the `clean.bat`
removes all the build artifacts again). In order to run the script, you need to
add the following things to the build folder:

###### Java Runtime Environment (JRE)

We package a JRE together with the application. Just download the
[OpenJDK 8 JRE](https://adoptopenjdk.net/)
for Windows 64 bit (e.g. `jre-8u141-windows-x64.tar.gz`), extract it, and
copy the content into the folder `build/jre/win64`.

###### 7zip
For packaging the applications we use [7zip](http://www.7-zip.org/download.html).
Just download the non-installer version and copy the (64bit) `7za.exe`
directly into the build folder.

##### macOS

To build on macOS, it is necessary to create a build path variable `SWT_LIB` and 
point it to 
`Eclipse.app/Contents/Eclipse/plugins/org.eclipse.swt.cocoa.macosx.x86_64_3.106.3.v20180329-0507.jar`
in order for the application to run.

To create an application bundle for distribution, run the export wizard with the above
settings. The result will be written to a folder `eclipse` in the selected export folder 
and will not run on macOS out of the box. In order to repair the build, run the
`repair4mac.sh` script giving the full or relative path to the `eclipse` folder above as 
an argument.

In order for the application to run under macOS, currently the Java 8 JDK needs to be 
installed on the target system.


#### Validation profile
... 

...

## License
Unless stated otherwise, all source code of the openLCA project is licensed
under the [Mozilla Public License, v. 2.0](http://mozilla.org/MPL/2.0/). Please
see the [LICENSE.md](./LICENSE.md) file in the root directory of the source code.

