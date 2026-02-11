# AAS DSP Discovery Extension

This extension discovers available data assets from data providers using the Data Space Protocol (DSP).

## Configuration

```yaml
discovery:
  implementation: de.fraunhofer.iosb.ilt.dataspace_consumer.aas_dsp_discovery_extension.DiscoveryImpl
  config:
    baseUrl: https://edc-control-plane/management  # DSP provider's control plane URL
```

**Key Parameter:**
- `baseUrl`: Your edc control plane management endpoint URL
