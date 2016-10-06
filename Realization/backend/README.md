# CzechIdM backend

Java application deployable to Tomcat 8 application server.

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

## Installation

* Install `Tomcat 8.0.*`, `JDK 8` and `maven` (at least 3.1 is required)
* build all mandatory project modules in order above (`mvn clean install`) + deploy `idm.war` file located in `app` module target folder to Tomcat.

## Configuration

Default profile is using h2 database. It is not nessesary a configuration for first start.

* TODO: profiles, db and other props

## Demo user credentials

* username: `admin`
* password: `admin`
