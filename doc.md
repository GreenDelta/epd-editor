# Some technical details of the EPD-Editor

## The workspace and the index
By default the EPD-Editor stores its data into the `~/.epd-editor` folder. The
ILCD data sets are directly stored as XML files under the `~/.epd-editor/ILCD`
folder. Also, server connections, validation profiles, application settings etc.
are saved in their respective sub-folders and files in a plain text format (ILCD
data sets in XML; the other things in JSON). As we currently do not use a real
database we also store a data set index with the meta-data of the data sets in
an `index.json` file. This is then mapped to the navigation tree and supports
quick data set access and searching.

For searching the usage of products in EPD data sets (and maybe later also
other things), we also cache the data set references in the
`~/.epd-editor/cache/refs` folder. 

## EPD Extensions
The EPD format adds some extensions to the process and flow data sets of the
ILCD format. These extensions can be read and written with the two converter
classes `ProcessExtensions` and `FlowExtensions`.
