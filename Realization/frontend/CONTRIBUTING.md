# Contributing to czechidm
We want to make contributing to this project as easy and transparent as possible.

TODO: Idea:
* https://developer.mozilla.org/en-US/docs/Mozilla/Projects/L20n/Contribute

## Issues
We use Redmine issues to track bugs. Please ensure your description is clear and has sufficient instructions to be able to reproduce the issue.

## Coding Style, konvence (draft)
* ES6
  * použití tříd
  * používat šipkovou syntaxi pro předávání funkcí (odpadá nutnost použití `.bind(this)`)
  * použít `import` - import knihoven třetích stran jest uveden před vlastními
  * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array
  * Immutable
  * ...
* Use semicolons;
* Commas last,
* 2 spaces for indentation
* Prefer `'` over `"`
* 80 character line length
* Do not use the optional parameters of `setTimeout` and `setInterval`
* Use JSDoc for documentation https://developers.google.com/closure/compiler/docs/js-for-compiler
* Make tests (mocha, chai)
* Při porovnání hodnot používat typové porovnání (`!==`, `===`)
* Konvence [návrhu databáze](https://proj.bcvsolutions.eu/ngidm/doku.php?id=navrh:identifikatory)
* Entity musí obsahovat validační annotace dle DB tabulky (jsr303)
* Restové endpointy nazýváme vždy v množném čísle s pomlčkami (e.g. `wf-identities`)
* objekty v jsonu pojmenováváme jako v javě - camelCase, "pomocné" atributy s podtržítkem na začátku (_links, _trimmed ...)
* `constructor` metoda musí býti na začátku třídy
* `componentDidMount` a další react metody by měly být v pořadí dle životního cyklu
* `render` metoda musí býti na konci react komponenty
* Znak `_` na začátku metod či proměnných v javascriptu značí privátní metodu či proměnnou

## IDE

We are using [Atom IDE](https://atom.io/) for development with plugins installed:
* linter
* linter-eslint
* react
* linter-bootlint
* atom-bootstrap-3
* git-diff-details
* git-diff-popup
* git-plus
* git-status
* line-diff-details
* last-cursor-position
* docblockr

Tips for Atom:
* **Ctrl+Shift+P** Search any function of Atom.
* **Ctrl+R** Search method in current file.
* **Auto indent** Formatting of selected code (jsx).



## Our Development Process

1. Clone or Fork the repo and create your branch from `CzechIdMng`.

2. If you've added code that should be tested, add tests to test folder.

3. Ensure the test suite passes.

4. Make sure your code lints.

5. Do not commit anything to the `dist` folder.

## License
TODO
