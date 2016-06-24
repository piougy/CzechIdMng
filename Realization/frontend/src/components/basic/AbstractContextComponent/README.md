# AbstractContextComponent Component

__Supports:__
* Automatically injects redux context (store) to component context,
* localization,
* add message to context.

## Usage

```javascript
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
...
export default class MyComponent extends AbstractContextComponent {
  ...
  // functions from AbstractContextComponent can be used (e.q i18n(..), addMessage(...) ...)
  ...
}
...
```
