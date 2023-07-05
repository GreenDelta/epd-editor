# EPD Editor
This is an editor for [ILCD](http://eplca.jrc.ec.europa.eu/LCDN/developer.xhtml)
data sets with [EPD format extensions](http://www.oekobaudat.de/en/info/working-group-indata.html). 

## Building from source
The EPD editor is an [Eclipse RCP](https://wiki.eclipse.org/Rich_Client_Platform)
application. To compile it from source you need to have the following tools
installed:

* a [Java Development Kit >= 14](https://adoptopenjdk.net/)
* [Maven](http://maven.apache.org/)
* the [Eclipse package for RCP developers](https://www.eclipse.org/downloads/)

When you have these tools installed you can build the application from source
via the following steps:

#### Install the openLCA core modules
The EPD Editor uses the current version of the 
[openLCA core modules](https://github.com/GreenDelta/olca-modules) for reading
and writing ILCD data sets. It is a plain Maven project and can be
installed via `mvn install`. See its documentation for more information.

#### Get the source code of the application
We recommend to use Git to manage the source code, but you can also download the
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
have no compile errors, and you should be able to open the `app.product` file
and launch the application (click on `Launch an Eclipse application`).

The target platform is configured for multi-platform builds as described
[here](https://stackoverflow.com/a/12737382). You may have to update the target
platform when setting up the development environment.

Also, when updating the target platform it is probably required to update the product
configuration in `app.product`. Just remove and re-add all required plugins. It is
important to set the start levels for the following plugins to these values:

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
The EPD-Editor uses the EPD profiles from the 
[ILCD Validation API](https://bitbucket.org/okusche/ilcdvalidation). 
Profiles need to be located under `validation_profiles`. A profile for the German
OEKOBAUDAT is automatically added to this location when dependencies are copied
using `mvn package` as described above under "Copy the Maven dependencies".

## License
Unless stated otherwise, all source code of the openLCA project is licensed
under the [Mozilla Public License, v. 2.0](http://mozilla.org/MPL/2.0/). Please
see the [LICENSE.md](./LICENSE.md) file in the root directory of the source code.

