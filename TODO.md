## TODO

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