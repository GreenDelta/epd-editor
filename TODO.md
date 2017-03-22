## TODO


* Download (including references from extensions)


* flows: allow flow type change... -> no just allow to create products but only
  show product information when type is PRODUCT?

* remove empty categories from navigation tree

* create Categories class in store package that manages access to category files
* re-index function
* show product unit in quantitative reference of EPD
* Source type combo in SourcePage
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


bug: catch error: open data set that does not exist

* type of data set currently not added
* collect & update data set references when opening a data set
* synchronize categories from server
* delete complete categories and data sets from navigation tree

* proxy settings
* support multiple versions 

---
doc:

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