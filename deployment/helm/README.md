# Dataspace Consumer Framework Helm Chart

This Helm chart deploys the Dataspace Consumer Framework on a Kubernetes cluster.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+
- PV provisioner support in the underlying infrastructure (for persistent storage)

## Installing the Chart

To install the chart with the release name `dataspace-consumer`:

```bash
helm install dataspace-consumer ./deployment/helm

# Or with specific namespace 
helm install dataspace-consumer ./deployment/helm --namespace custom-namespace
```

This command deploys the Dataspace Consumer Framework on the Kubernetes cluster with the default configuration.

## Uninstalling the Chart

To uninstall the `dataspace-consumer` deployment:

```bash
helm uninstall dataspace-consumer
```

This command removes all the Kubernetes components associated with the chart and deletes the release.

## Configuration
The chart can be customized by providing a `values.yaml` file or by using `--set` flags during installation. The following are some of the configurable parameters:

### Application Configuration

The application configuration is defined in the `config.application.yml` section of `values.yaml`. This file is mounted as a ConfigMap and loaded by Spring Boot at startup.

```yaml
config:
  application.yml: |
    # Custom config - Add your application configuration here
    spring:
      application:
        name: dataspace-consumer-framework 
        

    mx-port:
      - name: MX-Port1
        discovery:
          implementation: de.fraunhofer.iosb.ilt.faaast.service.AasDiscovery
          config:
            aasServer: http://localhost:8080
```

See the [application configuration documentation](../../README.md#configuration) for details.

### Add Extensions

To add extensions to the Dataspace Consumer Framework:

1. **Enable extensions** by setting `enabled: true`:
```yaml
extensions:
  enabled: true
```

2. **Configure the download source** - the base URL where extensions are hosted:
e.g., to download from GitHub Releases:
```yaml
  source: "https://github.com/FraunhoferIOSB/dataspaceconsumer-framework/releases/download/v0.0.1"
```

3. **Add extension files** with `filename` (local name) and `url` (path to append to source):
```yaml
  files:
    - filename: "aas-dsp-discovery-extension.jar"
      url: "/aas-dsp-discovery-extension.jar"
    - filename: "console-adapter-extension.jar"
      url: "/console-adapter-extension.jar"
```

The init container will download each extension from `source + url` and save it as `filename`.

### Install with custom values file

```bash
helm install dataspace-consumer ./deployment/helm -f custom-values.yaml
```

### Upgrade the release

```bash
helm upgrade dataspace-consumer ./deployment/helm
```

### Enable Ingress

```yaml
ingress:
  enabled: true
  className: nginx
  hosts:
    - host: dataspace-consumer.example.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: dataspace-consumer-tls
      hosts:
        - dataspace-consumer.example.com
```

### Configure Resources

```yaml
resources:
  limits:
    cpu: 1000m
    memory: 1Gi
  requests:
    cpu: 500m
    memory: 512Mi
```
