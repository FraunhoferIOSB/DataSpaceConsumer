# Simple Converter Extension

This extension converts the list of AAS data (in JSON format) into JSON format using simple extraction operations.

This extension supports only JSON data as input and will return the data in JSON format too.

## Configuration

```yaml
converter:
  implementation: de.fraunhofer.iosb.ilt.dataspace_consumer.simple_converter_extension.SimpleConverter
  config:
    operation: indexAt              # Extract element at specified index
    value: 0                         # Index position (0 = first element)
```

**Key Parameters:**
- `operation`: Conversion operation, supported:
  - `indexAt`: Extract element at specified index
  - `merge`: Merge the list of AAS data into a single JSON object
- `value`: Operation-specific parameter (e.g., array index number)
