# FAAAST Gate Extension

This extension accesses Asset Administration Shell (AAS) data from repositories using the FAAAST Client libary.

## Configuration

```yaml
gate:
  implementation: de.fraunhofer.iosb.ilt.dataspace_consumer.faaast_gate_extension.GateImpl
  config: {}  # No additional configuration required
```

The FAAAST Client automatically connects to AAS repositories and retrieves asset data based on the data transfer authorization.
