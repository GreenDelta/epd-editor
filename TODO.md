## TODO

* validation of empty workspace throws error
* translation view: show `(no contents yet) | (noch kein Inhalt)` for empty fields.
* support multiple validation profiles (add, set default etc.)
* zip-export in old EPD plugin -> support data exchange
* add default material properties


* re-index function
* Source type combo in SourcePage
* Local search
* Application settings: application language, RAM
* fetch also categories from elementary flows in indexing etc.
* cross platform builds and tests; + default data package
* externalize more strings
* cleanup: re-index on errors or cancellation 
* allow editing of indicator mappings (best would be probably a simple wizard)
* Data set export with dependencies as zip package


bug: catch error: open data set that does not exist

* type of data set currently not added
* collect & update data set references when opening a data set
* synchronize categories from server
* delete complete categories and data sets from navigation tree

* proxy settings

## Classification synchronization
It is possible to update the classification system from a server. However,
the update URL is currently a fix string:

```java
public class ClassificationSync {
    ...
    String urlSpec = con.url + "/categorySystems/OEKOBAU.DAT";
    ...
}
```

There is currently no function in soda4LCA available for getting all
classification systems (it would be also nice to have similar functionalities
for locations). 

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