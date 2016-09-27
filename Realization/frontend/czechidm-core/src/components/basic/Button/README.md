# Button Component

Standard button as react component. Supports all standard button properties and adds some features and new parameters.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| level | oneOf(['default', 'success', 'warning', 'info', 'danger', 'link', 'primary'])  |  Control css / color  |   'default' |
| hidden  | bool | adds css hidden | false |
| showLoadingIcon  | bool | When showLoading is true, then showLoadingIcon is shown | false |
| showLoadingText  | string | When showLoading is true, this text will be shown. If is not defined, then children will be shown | children |

## Usage

```html
<Basic.Button level="success" onClick={() => { alert('clicked') }} showLoading={false}>Click me</Basic.Button>
```
