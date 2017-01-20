## TODO

* high performance import with re-indexing

* Download (including references from extensions)
* support multiple versions 

* show product unit in quantitative reference of EPD
* Source type combo in SourcePage
* updates of data sets in navigation tree (clean empty categories)
* Navigator.refresh -> remember expansion state (like in openLCA)
* Local search
* Application settings: data set language
* LCIA editor
* flow editors: show extensions only for product flows; rename navi labels

bug: scenario table: org.eclipse.core.runtime.AssertionFailedException: assertion failed: Column 0 has no label provider
bug: close editors for deleted data sets
bug: close connection editor when connection is deleted

* sources: open external files on double click
* allow editing of indicator mappings (best would be probably a simple wizard)

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
