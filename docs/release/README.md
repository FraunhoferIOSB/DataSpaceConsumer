# Release Process

## Overview

Each module has its own independent version number:
- `bom/pom.xml` - BOM version
- `api/pom.xml` - API version
- `framework/pom.xml` - Framework version
- `extensions/pom.xml` - Extensions version

## Preparation

1. Ensure all tests on `main` have passed
2. Verify that all changes are committed and pushed

## Performing a Release

### Option 1: Release a Single Module

#### 1. Update version in the module's `pom.xml`

Change the version from `X.X.X-SNAPSHOT` to `X.X.X`:

```bash
# Example: Update api version to 2.1.5
cd api
mvn versions:set -DnewVersion=2.1.5
mvn versions:commit
```

Keep in mind to update the other pom.xml also. Changes to:
- bom version, update api, framework, extensions version in properties
- api version, update framework, extensions version in properties
- framework version, no other version updates
- extensions version, no other version update


#### 2. Commit changes

```bash
git add . && git commit -m "Release api version 2.1.5"
```

#### 3. Create release tag

Use a module-specific tag:

```bash
git tag api-v2.1.5
git push origin api-v2.1.5
```

The pipeline is triggered and publishes only the `api` release to GitHub Packages.

#### 4. Prepare next development version

```bash
mvn versions:set -DnewVersion=2.1.6-SNAPSHOT -pl api
mvn versions:commit
git add . && git commit -m "Bump api to 2.1.6-SNAPSHOT"
git push
```

### Option 2: Release All Modules (Synchronized Release)

If you need to release everything at once:

#### 1. Update all versions

```bash
mvn versions:set -DnewVersion=1.0.0
mvn versions:commit
```

#### 2. Commit and tag

```bash
git add . && git commit -m "Release version 1.0.0"
git tag v1.0.0 && git push origin v1.0.0
```

#### 3. Bump to next SNAPSHOT

```bash
mvn versions:set -DnewVersion=1.0.1-SNAPSHOT
mvn versions:commit
git add . && git commit -m "Bump version to 1.0.1-SNAPSHOT"
git push
```

## Module-Specific Release Examples

```bash
# Release bom
mvn versions:set -DnewVersion=1.0.0 -pl bom
git tag bom-v1.0.0 && git push origin bom-v1.0.0

# Release api
mvn versions:set -DnewVersion=2.1.5 -pl api
git tag api-v2.1.5 && git push origin api-v2.1.5

# Release framework
mvn versions:set -DnewVersion=3.0.2 -pl framework
git tag framework-v3.0.2 && git push origin framework-v3.0.2

# Release extensions
mvn versions:set -DnewVersion=1.2.0 -pl extensions
git tag extensions-v1.2.0 && git push origin extensions-v1.2.0
```

## Notes

- The release is published automatically by the GitHub Actions pipeline based on the tag pattern
- Module-specific tags (e.g., `api-v2.1.5`) trigger only that module's publish job
- Generic tags (e.g., `v1.0.0`) trigger all module releases
