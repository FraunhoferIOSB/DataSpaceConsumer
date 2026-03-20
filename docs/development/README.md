# Development Overview

This short document summarizes the important information extracted from the project's pom.xml files to help developers build and run the project locally.

## Important POMs

- [`bom/pom.xml`](bom/pom.xml:1) — central BOM and dependency management.
- [`framework/pom.xml`](framework/pom.xml:1) — Spring Boot application module. This is the main module which executes the mx-ports.
- [`api/pom.xml`](api/pom.xml:1) — API interfaces used by extensions.
- [`extensions/pom.xml`](extensions/pom.xml:1) — aggregator for extension modules 

## Notes

- Java version: 21 (see the POM properties).
- Formatting and checks: Spotless is configured and executed in the validate phase across modules.
- Extensions are built as PF4J plugins; the aggregator and module POMs configure assembly/manifest entries for plugin metadata.

## Running the Application

### BOM
To build the BOM, execute the following command from the project root:

```bash
mvn clean install
```

### API
To build the API module, execute the following command from the project root:

```bash
cd api

mvn spotless:apply

mvn clean install
```

### Extensions
To build all extensions, execute the following command from the project root:
```bash
cd extensions

mvn spotless:apply

mvn clean package

# Build only single module, e.g. rest-adapter-extension
mvn clean package -pl rest-adapter-extension
```

### Framework

First copy the extensions jars to the framework extensions directory by executing the following scripts:

```bash
# Windows
cd scripts 
.\copy-jars.ps1

## Linux
bash ./scripts/copy-jars.sh
```
---
To build the framework module, execute the following command from the project root:

```bash
cd framework

mvn spotless:apply

mvn spring-boot:run
# Or run with application-dev.yaml 
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"

# Bundle the application into a jar file
mvn clean package
```
---
To debug the framework with the extensions set log level to DEBUG:
```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--logging.level.root=DEBUG"
```
Or add the following to the `application.yaml`:
```yaml 
logging:
  level:
    root: DEBUG
```

### Running spotless 
To run spotless checks across all modules, execute the following command from the project root:

```bash
# Run spotless apply
mvn spotless:apply

# Run spotless checks
mvn spotless:check 
```


## Use maven wrapper
To use the maven wrapper, execute the commands with:

```bash
# From project root 
mvnw <command>

# From module directory, e.g. framework
..\mvnw <command>
```

