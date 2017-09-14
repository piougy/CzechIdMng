# Label Component

Shows label (highlighted text)

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| level | oneOf(['default', 'success', 'warning', 'info', 'danger', 'error', 'primary'])  |  Control css / color  |   'info' |
| text  | string   | Label text | |
| value  | string   | Label text (text alias - text has higher priority) | ||

## Usage

```html
<Label level="success" text="Everything is OK"/>
```
