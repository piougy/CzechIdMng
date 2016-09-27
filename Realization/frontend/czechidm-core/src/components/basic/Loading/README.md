# Loading Component

Visualize loading / "working". Its used as overlay.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| showLoading  | bool   | Shows loading overlay | false |
| showAnimation | bool    | When loading is active, shows animation too   |   true |
| loadingTitle | string | Shows title, when loading is active | Zpracovává se ... |

## Usage

```html
<Loading showLoading={true} showAnimation={true}>
  ...
    children
  ...
</Loading>
```
