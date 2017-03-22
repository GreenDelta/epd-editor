# EPD Editor
This is an editor for [ILCD](http://eplca.jrc.ec.europa.eu/LCDN/developer.xhtml)
data sets with [EPD format extensions](http://www.oekobaudat.de/en/info/working-group-indata.html). 

## Building from source
The EPD editor is an [Eclipse RCP](https://wiki.eclipse.org/Rich_Client_Platform)
application. To compile it from source you need to have the following tools
installed:

* a [Java Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven](http://maven.apache.org/)
* the [Eclipse package for RCP developers](https://www.eclipse.org/downloads/)

When you have these tools installed you can build the application from source
via the following steps:

#### Install the openLCA core modules
The EPD Editor uses the current version of the 
[openLCA core modules](https://github.com/GreenDelta/olca-modules) for reading
and writing ILCD data sets. These modules are plain Maven projects and can be
installed via `mvn install`. See the 
[olca-modules](https://github.com/GreenDelta/olca-modules) repository for a
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

## License
Unless stated otherwise, all source code of the openLCA project is licensed
under the [Mozilla Public License, v. 2.0](http://mozilla.org/MPL/2.0/). Please
see the [LICENSE.md](./LICENSE.md) file in the root directory of the source code.

