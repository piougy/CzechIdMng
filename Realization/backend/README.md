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
mvn clean install -PfullApp
```
which build whole application - backend and frontend in one `idm.war` file (require gulp installation - see [frontend installation guide](../frontend/README.txt)).
Deploy `idm.war` package is the same as above.

## Demo user credentials

* username: `admin`
* password: `admin`

## Configuration

Default profile is using h2 database. It is not nessesary a configuration for first start.

* TODO: other jdbc bc, profiles, db and other props
