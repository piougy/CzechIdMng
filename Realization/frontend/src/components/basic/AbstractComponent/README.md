# AbstractComponent Component

Super class for all components in application.

## Common parameters

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| rendered  | bool |  | true |
| showLoading  | bool | Shows loading icon  (fa-refresh fa-spin) | false |

## Usage

```javascript
import AbstractComponent from '../AbstractContextComponent/AbstractComponent';
...
export default class MyComponent extends AbstractComponent {
  ...
  // functions from AbstractComponent can be used here
  ...
}
...
```
