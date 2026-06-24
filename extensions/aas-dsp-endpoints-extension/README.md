# AAS DSP Endpoint Extension

This extension provides a discovery implementation for AAS DSP-style endpoints.

Instead of querying a catalog or DSP service, all discovery results are **preconfigured in the YAML configuration** and directly used to generate access and gate requests.

This is useful for:
- fixed environments
- testing setups
- deployments without a catalog service

---

## Configuration

```yaml
discovery:
  implementation: de.fraunhofer.iosb.ilt.dataspace_consumer.aas_dsp_endpoints_extension.DiscoveryImpl
  config:
    endpoints:
      - assetId: 2025-1
        dspEndpoint: https://edc-control-plane/dsp
        assetURL: https://provider/assets/2025-1
        interfaceType: AAS-REPOSITORY
```

## Key Parameters

**`endpoints`**
- List of statically defined discovery entries that replace a dynamic DSP catalog lookup. Each containing the parameters below.

**`assetId`**
- Unique identifier of the asset used for DSP filtering, deduplication, and mapping access responses to the correct endpoint.

**`dspEndpoint`**
- DSP control plane URL where access requests for the corresponding asset are sent.

**`assetURL`**
- Direct HTTP endpoint used in the final gate request to retrieve the asset content after successful authorization.

**`interfaceType`**
- Identifier describing the type of interface exposed by the asset, forwarded to downstream components for correct handling.