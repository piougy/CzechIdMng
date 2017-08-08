# Tests in czechidm

__Technologies:__

* [React Test Utils](https://facebook.github.io/react/docs/test-utils.html)
* [Mocha](https://mochajs.org/#getting-started)
* [Chai](http://chaijs.com/api/bdd/)

__Other docs:__

* http://www.hammerlab.org/2015/02/14/testing-react-web-apps-with-mocha/
* http://www.asbjornenge.com/wwc/testing_react_components.html
* http://redux.js.org/docs/recipes/WritingTests.html
* http://reactkungfu.com/2015/07/approaches-to-testing-react-components-an-overview/
* https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/find

__Konvence (draft)__
* Don't use arrow functions in tests._Their lexical binding of the this value makes them unable to access the Mocha context, and statements like this.timeout(1000); will not work inside an arrow function._
* Use shallow renderer (`TestUtils.createRenderer()`), if its sufficient (no need for call method on rendered element) - `DOM` init and clean up is not needed.

__TODO__
* Tests with `ref` don't work within any renderer - find the way.
