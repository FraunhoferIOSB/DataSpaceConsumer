# Docker Deployment

This directory contains the Docker Compose deployment configuration for the Dataspace Consumer Framework.

## Quick Start

1. **Download or build the extensions**:
You can either download the pre-built extensions from the GitHub Release page (or latest action run) or build them locally using Maven.
- Download:
Go to the [Releases](https://github.com/FraunhoferIOSB/DataSpaceConsumer/releases) page and download the latest extension JARs.
Copy the downloaded JARs into this [extensions](./extensions) directory.

- Build: 
   ```bash
    cd extensions
    mvn clean package
    cp extensions/target/*-all.jar ./extensions/
   ```

3. **Start the application**:
   ```bash
    cd deployment/docker
    docker-compose up -d
   ```

## Configuration

### Docker Compose

The `docker-compose.yaml` defines the dataspace-consumer service with:
- Pre-built image from GitHub Container Registry
- Port 8080 exposed for REST API access
- Mounted volumes for extensions, configuration, and persistent data
- Automatic restart policy

### Application Configuration

The `config/application.yml` contains the MX-Port configuration.
See the [application configuration documentation](../../README.md#configuration) for details.

## Using Local Build

To use a locally built image instead of the pre-built one, uncomment the build section in `docker-compose.yaml`:

```yaml
build:
  context: ../..
  dockerfile: framework/Dockerfile
image: dataspace-consumer-framework:latest
```


## Volumes

- **extensions**: Read-only mount of plugin JAR files
- **config**: Read-only mount of application configuration
- **data**: Persistent volume for application data and logs

## Logs

View application logs:
```bash
docker-compose logs -f dataspace-consumer
```

## Stopping the Application

```bash
docker-compose down
```

To also remove persistent data:
```bash
docker-compose down -v
```
