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
* http://redux.js.org/docs/recipes/WritingTests.html

__Konvence (draft)__
* Není doporučováno v testech používat šipkovou syntaxi pro předávání funkcí
(_Their lexical binding of the this value makes them unable to access the Mocha context, and statements like this.timeout(1000); will not work inside an arrow function._)
* Použití shallow rendereru (_TestUtils.createRenderer()_) pokud dostačuje (pokud není potřeba volat funce na elementem) - není pro nj třeba inicializovat a čistit dom.
* ~~Pokud nedostačuje shallow renderer, je třeba po každém testu uklidit (_viz. afterEach ... ReactDOM.unmountComponentAtNode(document.body) ..._)~~

__TODO__
* Testy komponent vyžadující property `ref` nefungují za požití žádného rendereru - najít způsob.
