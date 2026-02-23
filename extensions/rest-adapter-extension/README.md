# REST Adapter Extension

This extension forwards converted data to external REST endpoints for persistent storage or further processing.

It supports POST requests to send data to the specified endpoint. The adapter can be configured with the base URL of the target REST API, and it will handle the communication accordingly.

## Configuration

```yaml
adapter:
  implementation: de.fraunhofer.iosb.ilt.dataspace_consumer.rest_adapter_extension.RestAdapter
  config: 
    - baseUrl: https://endpointUrl
```

The REST adapter enables integration with external systems via HTTP requests.
