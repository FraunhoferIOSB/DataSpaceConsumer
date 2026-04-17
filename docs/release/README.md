# Release Process

## Overview

Each module has its own independent version number:
- `bom/pom.xml` - BOM version
- `api/pom.xml` - API version
- `framework/pom.xml` - Framework version
- `extensions/pom.xml` - Extensions version

## Versioning
- Version without SNAPSHOT: Release version
- Version with SNAPSHOT: Development version after release
Make sure to push the new -SNAPSHOT version commit after the release commit!

## Preparation

1. Ensure all tests on `main` have passed
2. Verify that all changes are committed and pushed

## Performing a Release

If you need to release everything at once:

#### 1. Update all versions

```bash
mvn versions:set "-DnewVersion=1.0.0"
mvn versions:commit
```

#### 2. Commit and tag

```bash
git add .
git commit -m "Release version 1.0.0"
git tag v1.0.0
git push origin v1.0.0
```

#### 3. Bump to next SNAPSHOT

```bash
mvn versions:set -DnewVersion=1.0.1-SNAPSHOT
mvn versions:commit
git add .
git commit -m "Bump version to 1.0.1-SNAPSHOT"
git push
```
