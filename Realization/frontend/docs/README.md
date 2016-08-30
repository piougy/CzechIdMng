# CzechIdM frontend

Javascript client for CzechIdM.

## Modules
* core - core functionality - required module
  * `components` - elementary (basic) and complex (advanced) components can be used in concrete pages (content).
  * `domain` - simple classes to encapsulate some domain objects (e.g. SearchParameters)
  * `enums` - contains module enumerations
  * `utils` - static helper methods for entitites, ui etc.
  * `services` - business logic, rest calls, resolve localization
  * `redux` - holds application state and provide actions (encapsulated to managers) to change application state (managers could call services)
  * `content` - pages
  * `themes` - css, images for concrete theme (any module could register theme)
  * `locales` - module scoped locales
  * ...

## Basic Components

All components could be found in libraries:

* [basic](../src/components/basic/) - basic "simple" components. Mainly without knowing about application content, localization etc. Contextual and other parameter can be setted through props.
* [advanced](../src/components/advanced/) - advanced "context" components. Basic component compositions with some logic, context listeners etc.

### Common component super classes

All components are descendants of one of classes:

* [AbstractComponent](../src/components/basic/AbstractComponent/README.md)
* [AbstractContextComponent](../src/components/basic/AbstractContextComponent/README.md)
* [AbstractContent](../src/components/basic/AbstractContent/README.md)

### Form component super classes

All forms are descendants of one of classes:

* [AbstractForm](../src/components/basic/AbstractForm/README.md)

All components used in forms (e.g. TextField, Checkbox ..) are descendants of one of classes:

* [AbstractFormComponent](../src/components/basic/AbstractFormComponent/README.md)
