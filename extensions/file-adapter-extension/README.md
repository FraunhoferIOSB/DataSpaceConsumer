# File Adapter Extension

This extension writes the retrieved data to a file for testing and debugging purposes.

## Configuration

```yaml
adapter:
  implementation: de.fraunhofer.iosb.ilt.dataspace_consumer.console_adapter_extension.ConsoleAdapter
  config: 
    folderPath: "example-directory"                       # Path relative to the project root. 
```

**Key Parameters:**
- `folderPath`: Path of the output file, relative to the project's root. The data will be saved in a file named "payload". The extension depends on the data type.