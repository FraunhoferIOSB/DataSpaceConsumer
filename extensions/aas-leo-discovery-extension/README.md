# AAS LEO Discovery Extension

This extension discovers Asset Administration Shell (AAS) data assets exposed by data providers via a LEO-based discovery service. It queries a discovery endpoint, parses the response, and returns a list of discoverable assets.

## Features

- Query a LEO discovery service for available AAS assets
- Parse discovery responses into ResultItem objects
- Configurable discovery endpoint and optional domain scoping

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

- `discoveryBaseUrl`: The base URL of the discovery API to query. Example: `https://discovery.example.com/api`
- `domain`: A domain or namespace used by the discovery service to scope results. Leave empty to search across all domains.

## Usage

Add the configuration above to your DataSpaceConsumer configuration. The extension will call the configured discovery endpoint, parse the results and provide a list of assets represented as `ResultItem` instances.

## Implementation details

The extension contains the following classes (package: `de.fraunhofer.iosb.ilt.dataspace_consumer.aas_leo_discovery_extension`):

- `DiscoveryImpl` — main discovery implementation
- `DiscoveryRequestFactory` — builds discovery requests
- `AasDiscoveryParser` — parses discovery responses
- `ResultItem` — DTO for discovered assets

Adjust configuration values to match the discovery service you integrate with.

License: Use the repository's root license.
