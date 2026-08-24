# JSON REST Gate Extension

This extension accesses JSON data from repositories using an OkHttp Client.

## Configuration

```yaml
gate:
  implementation: de.fraunhofer.iosb.ilt.dataspace_consumer.json_rest_gate_extension.GateImpl
  config: {
            "endpoint": "https://your-custom-endpoint" # Optional
  }
```

The HTTP Client automatically connects to JSON http endpoints and retrieves asset data based on the data transfer
authorization. It also adds the original access token and URL to the GateResponse for further communication with the
data provider endpoint.
