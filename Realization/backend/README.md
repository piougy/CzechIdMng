# CzechIdM backend

Java application deployable to Tomcat 8 application server.

## Requirements

* Install `JDK 8`
* Install `maven` - at least version `3.1` is required
* Install `Tomcat 8.0.*`, tested versions:
  * 8.0.24
  * 8.0.35
  * 8.0.36

## Project modules [mandatory]
* `parent` - maven parent of all following submodules with common dependencies.
* `core` - contains base idm functionality (entities, repositories, services etc.).
  * `core-api` - core interfaces
  * `core-test-api` - test interfaces
  * `core-impl` - core services, entities and rest implementation
* `app` - contains example .war application - all modules (core + business modules) and their configuration.

### Application modules [optional]
* `acc` - Account management module
* `module-example` - Example application module / skeleton

### Maven modules [optional]
* `gui` - frontend as .war package
* `module-aggregator` - complex builder of all modules

## Build and deploy

Build all mandatory project modules in order above (`mvn clean install`) + deploy `idm-backend.war` file located in module `app` target folder to Tomcat.

The quickest way, how to build whole backend application, is to use `module-aggreagator`. In module-aggreagator folder, where pom.xml is located run command:

```
mvn clean install
```
or

```
mvn clean install -Prelease
```
which build whole application - backend and frontend in one `idm.war` file (require gulp installation - see [frontend installation guide](../frontend/README.txt)).
Deploy `idm.war` package is the same as above.

## Development

### Setup jpa metamodel generation in Eclipse

This setup has to be done for modules **core-api**, **core-impl** and other optional modules, which uses criteria api (e.g. **acc**).

* Go to `Project` -> `Properties` -> `Java Compliler` -> `Annotation Processing` -> check `Enable project specific settings` and fill **target/metamodel** as `Generated source directory`.
* Go to `Project` -> `Properties` -> `Java Compliler` -> `Annotation Processing` -> `Factory path` -> check `Enable project specific settings` and add external jar `hibernate-jpamodelgen.jar` (version 5.x.x). Artefact could be found in local maven repository or downloaded from any public maven repository.
* remove Eclipse pom.xml error - `Plugin execution not covered by lifecycle configuration: org.bsc.maven:maven-processor-plugin:3.3.1:process (execution: process, phase: generate-sources)` - go to `Window` -> `Preferences` -> `Maven` -> `Errors/Warnings` -> set `Plugins execution not covered by lifecycle configuration` to `warning`.

[Other IDEs](https://docs.jboss.org/hibernate/jpamodelgen/1.0/reference/en-US/html_single/#d0e319)

### Setup jpa metamodel generation in IntelliJ Idea

* Enable annotation procesors according to guide above. It does not matter where you generate sources to. Leave same path (generated-sources) but make sure to leave `Store generated sources relative to` set to `Module output directory`. Otherwise they wont be compiled and deployed.
* Go to `File` -> `Project structure` -> `Libraries` and click on green plus sign in top portion of the window. Select hibernate-jpamodelgen.jar` (version 5.x.x).
* In project view (Alt + 1) find folder with generated metamodel in each project and click right on in and select `Mark directory as` -> `Generated sources root`. Now Idea knows about metamodel and you can use it in code without typecheck errors

## Demo user credentials

* username: `admin`
* password: `admin`

## Configuration

Default profile is using h2 database. It is not nessesary a configuration for first start.

All configuration properties can be found in [documentation](https://proj.bcvsolutions.eu/ngidm/doku.php?id=en:navrh:konfigurace_aplikace).
