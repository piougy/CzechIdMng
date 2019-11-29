# CzechIdM Tool

## Features
- Change product version - set version for all modules.
- Release product version - release product under final version, new development version will be set, tag will be prepared.

## Requirements

- Install `maven` - at least version `3.1` is required.

## How to build the tool

Standalone ``idm-tool.jar`` can be build under ``dist`` profile. In the tool module folder (checkout master branch is needed):

```bash
mvn clean package -Pdist
```

Executable ``jar`` will contain all required libraries (all-in-one).

## Tool parameters

Available tool parameters:

```bash
java -jar idm-tool.jar -h
```

Other commands examples expects ``idm-tool.jar`` is placed in the same folder, where the product root folder is.

### Release product

```bash
java -jar idm-tool.jar --release --releaseVersion 10.0.0 --developVersion 10.1.0-SNAPSHOT
```

Release product under ``releaseVersion`` argument.
New development version will be set as ``developVersion`` argument.

**Local commits in master and develop branches are prepared only.** Local tag with release version is prepared too. Publish release into origin repository (after personal verification, if everything looks great :)):

```bash
java -jar idm-tool.jar --publish --username <git username> --password <git password or developer token>
```

### Get product version

Get current product version.

```bash
java -jar idm-tool.jar --getVersion
```

### Change product version

Change product version for all product modules (FE + BE).
Versions are changes only in develop branch (without commit) => changed versions in modules can be re

```bash
java -jar idm-tool.jar --setVersion --developVersion 10.1.0-SNAPSHOT
```

### Revert product version

Changed versions by previous command can be reverted if needed. Module descriptors (pom.xml, package.json) are reverted only.

```bash
java -jar idm-tool.jar --revertVersion
```

## Future development

- release other module
- build project artefacts (~war):
  - download or use product in defined version
	- download or use project artefart
	- append configuration
	- build BE + FE => war

## License

[MIT License](../../../LICENSE)
