# Alert Component

Shows alert box

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| level | oneOf(['success', 'warning', 'info', 'danger'])  |  Control css / color  |   'info' |
| icon  | string   | glyphicon suffix name | if empty, then isn't rendered |
| title  | oneOfType([string, node])  | Alert strong title content | if empty, then isn't rendered |
| text  | oneOfType([string, node])   | Alert text | |
| onClose  | func   | Close function - if it's set, then close icon is shown and this method is called on icon click | | |

## Usage

```html
<Basic.Alert level="success" icon="ok" text="Identity john.doe was successfully saved." onClose={() => { alert('closed'); }}/>
```
