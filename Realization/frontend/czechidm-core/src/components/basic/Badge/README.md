# Badge Component

Shows badge (highlighted text)

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| level | oneOf(['default', 'success', 'warning', 'info', 'danger', 'error''])  |  Control css / color  |   'default' |
| text  | string   | Badge text | |
| value  | string   | Badge text (text alias - text has higher priority) | ||

## Usage

```html
<Badge level="success" text="Everything is OK"/>
```
