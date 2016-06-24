# Backend

Java application deployable to Tomcat 8 application server.

## Project modules
* `parent` - maven parent of all following submodules with common dependencies.
* `core` - contains base idm functionality (entities, repositories, services etc.).
* `client` - contains example .war application - all modules (core + business modules) and their configuration.

## Installation

* Install `Tomcat 8`, `JDK 8` and `maven` (at least 3.1 is required)
* Open all project in your favorite IDE (we are using eclipse).
* build + deploy `.war` file located in `client` module target folder to Tomcat

## Configuration
* TODO: configure default profile to h2 db
* TODO: db and other props
