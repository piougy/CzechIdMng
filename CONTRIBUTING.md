# Contributing to CzechIdM
We want to make contributing to this project as easy and transparent as possible.

## Issues
We use [Redmine](https://redmine.czechidm.com) to track bugs and enhancements.

### Bugs

If you believe you have found a bug, please take a moment to search the existing issues. If no one else has reported the problem, please open a new ticket that describes the problem in detail and has sufficient category and instructions to be able to reproduce the issue. Ideally, includes a test that reproduces it. Application, module (backend, frontend) and browser version has to be included in ticket description.

#### Strict ticket example:
| Name form attribute   |      Values/Info      |
|----------|:-------------:|
| Queue |  Task, Defect, Feature |
| Subject |    Clear, unambiguous and unique name, **don't create duplicate tickets please!** |
| Description | **Very detailed description**, bug or feature, with affected version. Description how to reproduce bug (use case, steps, etc.). StackTrace with error. Info why is new feature so necessary. |
| State | Please keep state as New |
| Priority | Remeber only critical bugs has priority higher than normal. For feature never set priority higher than normal. Features are planned for a very long time ahead. Is possible that your feature will be moved to another version. |
| Assigned | If you don't know who is responsible for category don't don't set up anyone. |
| Category | Best match category that describe your problem. |
| Target version | If you don't know what is current version, don't set up any version.  |
| Files | Please add some screens, your specific code and **tests** with bug. |
| Parent ticket, from, till, estimated time, done % | Please do not fill these attributes. |

If you found some other behavior feel free to add comment.

### Enhancements

If you’d like an enhancement to be made to CzechIdM, pull requests are most welcome. The source code is on GitHub. You may want to search the existing issues and pull requests to see if the enhancement is already being worked on. You may also want to open a new issue to discuss a possible enhancement before work on it begins.

## Backend

### Coding Style, convention

* https://google.github.io/styleguide/javaguide.html
* 1 tab for indentation
* Use `{}` brackets
* Don't use abbreviations for fields, variables etc.
* Don't use camelCase in package names
* Database
 * [Convention](https://wiki.czechidm.com/7.3/dev/conventions/database-conventions)
  * `Entity` / `Dto` has to contain jsr303 validations
* Rest
  * Rest endpoints naming with snake-case in plural, e.g. `<server>/wf-identities`, `<server>/tasks`
* Spring
  * use bean names, e.g. `@Service("identityService")`
  * use `@Transaction` annotation for service methods
  * use interfaces
    * `Configurable` for configurations
    * `Codeable` for entities with code and name - see database naming [convention](https://wiki.czechidm.com/7.3/dev/conventions/database-conventions)
    * ...
* Java
  * Class - fields first, then constructors, then public methods, then private methods

### IDE
* [Eclipse](https://wiki.czechidm.com/7.3/dev/quickstart/ide/eclipse)
* [Idea](https://wiki.czechidm.com/7.3/dev/quickstart/ide/idea)

## Frontend

### Coding Style, convention

* ES6
  * use classes
  * use arrow functions (`.bind(this)` is not needed)
  * use `import` - import third party libraries
  * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array
  * Immutable
  * ...
* Use semicolons;
* Use `{}` brackets
* Commas last,
* 2 spaces for indentation
* Prefer `'` over `"`
* 80 character line length
* Do not use the optional parameters of `setTimeout` and `setInterval`
* Use JSDoc for documentation (`@author` etc.) https://developers.google.com/closure/compiler/docs/js-for-compiler
* Make tests (mocha, chai)
* Use identity operator `!==`, `===`
* Naming convention for object is the same as in java - camelCase, "private" attributes and methods starting with `_`, e.g. `_links`, `_trimmed`
* `constructor` method has to be at the start of class
* `componentDidMount` and the next react lifecycle methods has to be in the lifecycle order.
* `render` method has to be on the end of react component
* Character `_` at the start of attribute or method => private attribute or method
* use less variables
* use `encodeURIComponent` to encode parameters used directly in urls e.g. ``this.context.router.push(`identity/${encodeURIComponent(entity.username)}/profile`)``

### IDE

* [Atom](https://wiki.czechidm.com/7.3/dev/quickstart/ide/atom)


## Our Development Process

1. Clone or Fork the repo and create your branch from `CzechIdMng`.

2. If you've added code that should be tested, add tests.

3. Ensure the test suite passes.

4. Make sure your code lints.

5. Do not commit anything to the `dist` or `target` folder.

## License

[MIT License](./LICENSE)
