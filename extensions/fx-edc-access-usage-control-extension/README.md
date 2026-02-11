# Factory-X EDC Access & Usage Control Extension

This extension manages access control and negotiates contracts with data providers using the Factory-X EDC (Eclipse Dataspace Connector).

## Configuration

```yaml
accessAndUsageControl:
  implementation: de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.AccessUsageControlImpl
  config:
    baseUrl: https://consumer-control-plane/management                # Consumer's control plane URL
    counterPartyAddress: https://provider-control-plane/api/v1/dsp    # Provider's DSP endpoint
    counterPartyId: did:web:provider-identity                         # Provider's DID
    apiKey: authentication-key                                        # API authentication key
```

**Key Parameters:**
- `baseUrl`: Consumer's EDC control plane management endpoint
- `counterPartyAddress`: Provider's DSP communication endpoint
- `counterPartyId`: Provider's decentralized identifier (DID)
- `apiKey`: Authentication credentials for consumer's EDC
