# AAS LEO Discovery Extension

This extension discovers Asset Administration Shell (AAS) data assets exposed by data providers via a LEO-based discovery service. It queries a discovery endpoint, parses the response, and returns a list of discoverable assets.

## Configuration

Example configuration (application.yaml):

```yaml
discovery:
  implementation: de.fraunhofer.iosb.ilt.dataspace_consumer.aas_leo_discovery_extension.DiscoveryImpl
  config:
    discoveryBaseUrl: "https://discovery.example.com/api" # Base URL of the discovery service (required)
    domain: "example-domain" # domain/namespace to restrict the search
```

Key parameters

- `discoveryBaseUrl`: The base URL of the discovery API to query.
- `domain`: A domain or namespace used by the discovery service to scope results.
