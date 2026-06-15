# Factory-X LEO Access Control Extension

This extension provides access control integration for Factory-X LEO-style token exchange. It performs the following operations:

- Requests a source token using the OAuth2 client credentials grant
- Exchanges the source token for a Factory-X (FX) token via a security token service (STS)

## Configuration

Example configuration (application.yaml):

```yaml
accessAndUsageControl:
  implementation: de.fraunhofer.iosb.ilt.dataspace_consumer.fx_leo_access_control_extension.AccessUsageControlImpl
  config:
    consumerCredentialServerUrl: "https://auth.example.com/oauth/token"  # Token endpoint for client_credentials (required)
    clientId: "consumer-client-id"                                      # OAuth2 client_id (required)
    clientSecret: "secret"                                              # OAuth2 client_secret (required)
    consumerStsUrl: "https://sts.example.com/token-exchange"            # STS endpoint for token-exchange (required)
```

Key parameters

- `consumerCredentialServerUrl` (required): URL of the token endpoint where the extension requests a source token using the client credentials grant.
- `clientId` (required): OAuth2 client identifier used for the client credentials request.
- `clientSecret` (required): OAuth2 client secret used for the client credentials request.
- `consumerStsUrl` (required): URL of the security token service (STS) endpoint used to exchange the source token for an FX token.
