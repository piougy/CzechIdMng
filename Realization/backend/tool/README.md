# CzechIdM Tool

## Features
- Release product version - release product under final version, new development version will be set, tag will be prepared.
- Release module version - release module under final version, new development version will be set, tag will be prepared.
- Change product version - set version for all modules.
- Get product version - for test reasons only.
- Build product version - for test reasons only.

## Requirements

- Install `maven` - at least version `3.1` is required (Configure ``MAVEN_HOME`` environment property or use ``mavenHome`` IdM tool argument).

## How to build the tool

Standalone ``idm-tool.jar`` can be build under ``dist`` profile. In the tool module folder (checkout master branch is needed):

```bash
mvn clean package -Pdist -DskipTests
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

**Nexus credential has to be configured in maven** for deploy artefacts into our private ``maven-releases`` repository.

Example of ``settings.xml``:

```xml
<settings>
  <servers>
    <!-- Nexus servers -->
    <server>
      <id>maven-releases/id>
      <username>username</username>
      <password>password</password>
    </server>
    <server>
      <id>maven-releases</id>
      <username>username</username>
      <password>password</password>
    </server>
    <server>
      <id>nexus</id>
      <username>username</username>
      <password>password</password>
    </server>
    <server>
      <id>nexus-public</id>
      <username>username</username>
      <password>password</password>
    </server>
  </servers>

  <pluginGroups>
     <pluginGroup>external.atlassian.jgitflow</pluginGroup>
  </pluginGroups>

  <profiles>
    <profile>
      <id>nexus-repo</id>
      <repositories>
        <repository>
          <id>maven-snapshots</id>
          <url>https://nexus.bcvsolutions.eu/repository/maven-snapshots/</url>
          <releases><enabled>false</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </repository>
        <repository>
          <id>maven-release</id>
          <url>https://nexus.bcvsolutions.eu/repository/maven-releases/</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>false</enabled></snapshots>
        </repository>
        <repository>
          <id>archetype</id>
          <url>https://nexus.bcvsolutions.eu/repository/maven-public-releases/</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>false</enabled></snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>nexus-repo</activeProfile>
  </activeProfiles>
</settings>
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

Changed versions by previous command can be reverted if needed (before commit, usable after change product version only). Module descriptors (pom.xml, package.json) are reverted only.

```bash
java -jar idm-tool.jar --revertVersion
```

### Build product

Available @since version 10.1.0.

Build product only under current develop version in develop branch.

```bash
java -jar idm-tool.jar --build
```

Maven ``install`` command is used, artifact will be installed into the local maven repository (=> usable as dependency for other module).

### Release module

Available @since version 10.1.0.

All commands above are available for standalone module too. The only difference is **added parameter** with **module identifier**:

```bash
java -jar idm-tool.jar --module idm-rec --release --releaseVersion 2.0.0 --developVersion 2.1.0-SNAPSHOT --username <git username> --password <git password or developer token>
```
then
```bash
java -jar idm-tool.jar --module idm-rec --publish --username <git username> --password <git password or developer token>
```

The command above expects ``idm-tool.jar`` is placed in the same folder, where the module root (repository) folder is and has the same name as module identifier. In example above is used recertification module identifier.

Module can contains one frontend module and one or more backend modules (e.g. parent + api + impl). Module structure has respect module archetype generator (``Realization`` folders, frontend module with ``czechidm-`` prefix etc.).

Standalone modules are most probably in private repository, so credentials are needed.

## Future development

- build project artefacts (~war):
  - download or use product in defined version
	- download or use project artefart
	- append configuration
	- build BE + FE => war

## License

[MIT License](../../../LICENSE)
