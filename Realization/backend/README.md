# CzechIdM backend

Java application deployable to Tomcat 8 application server.

## Project modules
* `parent` - maven parent of all following submodules with common dependencies.
* `core` - contains base idm functionality (entities, repositories, services etc.).
* `app` - contains example .war application - all modules (core + business modules) and their configuration.
* `gui` - frontend as .war package [optional]
* `module-aggregator` - complex builder of all modules [optional]
* `acc` - Account management module [optional]
* `module-example` - Example application module / skeleton [optional]

## Installation

* Install `Tomcat 8.0.*`, `JDK 8` and `maven` (at least 3.1 is required)
* Open all project in your favorite IDE (we are using eclipse).
* build (all project modules in order above) + deploy `.war` file located in `client` module target folder to Tomcat

## Configuration

Default profile is using h2 database. It is not nessesary a configuration for first start.

* TODO: profiles, db and other props

## Demo user credentials

* username: `admin`
* password: `admin`
