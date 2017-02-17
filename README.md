## TODO

* need a sync function for the navigation: Navigator.sync(Index) -> refresh + case X
* Name changes not updated in navigation:: Use Data.update(ds)
* test add/update/delete in different locations
* bug: close editors for deleted data sets

* flows: allow flow type change... -> no just allow to create products but only
  show product information when type is PRODUCT?

* create Categories class in store package that manages access to category files
* New data set action: find name of classification system from category files.
* re-index function
* Download (including references from extensions)
* show product unit in quantitative reference of EPD
* Source type combo in SourcePage
* updates of data sets in navigation tree (clean empty categories)
* Local search
* Application settings: data set language, application language, RAM
* fetch also categories from elementary flows in indexing etc.
* cross platform builds and tests; + default data package
* Data set export with dependencies as zip package
* externalize strings
* cleanup: re-index on failor or cancellation 
* sources: open external files on double click
* allow editing of indicator mappings (best would be probably a simple wizard)
* LCIA editor: show fields + editing (first remove getters and setters in core API)


bug: scenario table: org.eclipse.core.runtime.AssertionFailedException: assertion failed: Column 0 has no label provider
bug: close connection editor when connection is deleted
bug: catch error: open data set that does not exist

## For future versions
* proxy settings
* support multiple versions 

## EPD Extensions
The EPD format adds some extensions to the process and flow data sets of the 
ILCD format. These extensions can be read and written with the two converter
classes `ProcessExtensions` and `FlowExtensions`.

## Indicator mappings
The EN 15804 defines a set of LCI and LCIA indicators. We use these indicators 
in the editor and the user can just enter amount values for a selected indicator
(see `IndicatorResult`). However, in the extended ILCD process format these
indicator results are stored as extended exchanges or LCIA results with
references to flow data sets (for LCI indicators) or LCIA method data sets (for
LCIA indicators) and unit groups.

To map an indicator name (see also the `Indicator` enum) to these references
(flow UUID, or LCIA method UUID, and unit group UUID) the editor uses a set of
indicator mappings (see the class `IndicatorMapping`) which can be also
configured in the editor.
