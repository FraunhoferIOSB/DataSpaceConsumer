# How to write your own extension

This guide explains how to create a custom extension (Adapter, Gate, Converter, Access and Usage Control, or Discovery) for the Dataspace Consumer Framework.

## 1. Project Setup

### 1.1 Create a New Maven Project

Start by creating a new Maven project and use the provided `template-pom.xml` as your base `pom.xml`.

### 1.2 Customize the POM

Open the `pom.xml` and replace the following placeholders:

- **`<groupId>`**: Replace `your-group-id` with your organization's group ID (e.g., `com.example`)
- **`<artifactId>`**: Replace `your-artifact-id` with your plugin's artifact ID (e.g., `my-custom-adapter`)
- **`<name>`**: Replace `your-project-name` with a descriptive name (e.g., `My Custom Adapter Plugin`)
- **`<plugin.id>`**: Replace `your-plugin-id/your-artifact-id` with a unique plugin identifier, could be the artifactid (e.g., `com.example/my-custom-adapter`)
- **`<plugin.provider>`**: Replace `your-organization` with your organization name (e.g., `Example Corp`)
- **`<plugin.class>`**: Leave empty if you don't need a custom plugin class; otherwise specify the fully qualified class name of your `org.pf4j.Plugin` implementation


## 2. Implement an Extension Interface

Create a new Java class in `src/main/java` and implement one of the interfaces. Annotate it with `@Extension` so PF4J can discover it.

**Example: Creating a Custom Adapter**

```java
package com.example.adapter;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.Adapter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.AdapterPayloadType;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.AdapterRequest;
import org.pf4j.Extension;

import java.util.List;

@Extension
public class MyCustomAdapter implements Adapter {

    @Override
    public List<AdapterPayloadType> supportedPayloadTypes() {
        return List.of(AdapterPayloadType.TEXT, AdapterPayloadType.JSON);
    }

    @Override
    public void adapt(AdapterRequest request) {
        // Your logic to send data to an external system
        System.out.println("Adapting payload: " + request.getPayload());
    }
}
```

## 3. Adding Configuration Support

### 3.1 When to Use `Configurable`

Implement the `Configurable` interface when your plugin needs **custom configuration values** from the `application.yaml` file. This allows users to configure your plugin per MX-Port instance without modifying your code.

**Example use cases:**
- Endpoint URLs
- Authentication credentials
- Retry counts or timeout values
- Feature flags

### 3.2 Implement `Configurable`

Add the `Configurable` interface to your class and implement the `setConfiguration` method:

```java
package com.example.adapter;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.Adapter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.AdapterPayloadType;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.AdapterRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import org.pf4j.Extension;

import java.util.List;
import java.util.Map;

@Extension
public class MyCustomAdapter implements Adapter, Configurable {
    
    private String endpoint;
    private int maxRetries = 3;
    private String apiKey;

    @Override
    public void setConfiguration(Map<String, Object> config) {
        if (config == null) return;
        
        // Extract configuration values
        Object endpointObj = config.get("endpoint");
        if (endpointObj instanceof String) {
            endpoint = (String) endpointObj;
        }
        
        Object retriesObj = config.get("maxRetries");
        if (retriesObj instanceof Number) {
            maxRetries = ((Number) retriesObj).intValue();
        }
        
        Object keyObj = config.get("apiKey");
        if (keyObj instanceof String) {
            apiKey = (String) keyObj;
        }
    }

    @Override
    public List<AdapterPayloadType> supportedPayloadTypes() {
        return List.of(AdapterPayloadType.TEXT, AdapterPayloadType.JSON);
    }

    @Override
    public void adapt(AdapterRequest request) {
        // Use configured values
        System.out.println("Sending to " + endpoint);
        System.out.println("Max retries: " + maxRetries);
        System.out.println("Using API key: " + (apiKey != null ? "***" : "none"));
        
        // Your adapter logic here
    }
}
```

### 3.3 Configure in `application.yaml`

In the Dataspace Consumer Framework's `application.yaml`, reference your implementation and provide the configuration:

```yaml
mx-port:
  - name: my-custom-port
    adapter:
      implementation: com.example.adapter.MyCustomAdapter
      config:
        endpoint: "https://api.example.com/ingest"
        maxRetries: 5
        apiKey: "your-secret-key"
```

The framework will automatically call `setConfiguration(...)` with the values from the `config` section.

## 4. Build the Plugin

Run Maven to build your plugin JAR:

```bash
mvn clean package
```

This creates a JAR with all dependencies in the `target/` directory, named `your-artifact-id-<version>-all.jar`.

## 5. Deploy the Plugin

### 5.1 Copy to Extensions Directory

Copy the generated JAR file to the `extensions/` directory of your Dataspace Consumer Framework installation:

```bash
cp target/my-custom-adapter-0.0.1-all.jar /path/to/framework/extensions/
```

### 5.2 Reference in Configuration

Update the `application.yaml` to use your plugin by specifying the full class name in the `implementation` field:

```yaml
mx-port:
  - name: example-port
    adapter:
      implementation: com.example.adapter.MyCustomAdapter
      config:
        endpoint: "https://api.example.com"
```

## 6. Run

Start (or restart) the Framework. The framework will:
1. Scan the `extensions/` directory
2. Load your plugin JAR
3. Discover classes annotated with `@Extension`
4. Inject configuration via `setConfiguration()` if `Configurable` is implemented
5. Use your implementation when the corresponding MX-Port is executed

Check the logs to verify your plugin was loaded successfully.

## Summary

- **Use the `template-pom.xml`** and customize the placeholders
- **Implement one of the interfaces** of the api package
- **Add `@Extension`** annotation to make your class discoverable
- **Implement `Configurable`** when you need custom configuration from `application.yaml`
- **Build with Maven** (`mvn clean package`)
- **Deploy the JAR** to the `extensions/` directory
- **Configure** the plugin in `application.yaml` using the full class name

You now have a working plugin for the Dataspace Consumer Framework!