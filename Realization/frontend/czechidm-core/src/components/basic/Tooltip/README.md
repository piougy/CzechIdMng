# Tooltip component

Component for create nice looking tooltip. Extended from AbstractComponent.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| trigger  | array of string   | action or actions trigger tooltip visibility | hover, focus |
| value  | string   | Tooltip value / text |  |
| placement  | string   | Tooltip position | bottom |
| delayShow  | number   | A millisecond delay amount before showing the Tooltip once triggered.  |  | |

## Usage

```html
<Tooltip
  trigger={['click', 'hover']}
  ref="popover"
  placement="left"
  value="Help message.">
 />
```
