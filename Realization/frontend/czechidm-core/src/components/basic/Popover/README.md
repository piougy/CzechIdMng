# Popover component

Wrapped react bootstrap popover. Extended from AbstractComponent.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| level | oneOf(['default', 'warning']) | Popover level / css / class. One of default or warning | default |
| placement  | oneOf(['top', 'bottom', 'right', 'left'])  | Popover position top, bottom, right, left | bottom |
| title | string | Popover title |  |
| value  | string   | Popover value / text |  |
| trigger  | arrayOf(oneOf(['click', 'hover', 'focus']))  | Action or actions trigger popover visibility click ,hover , focus | hover, focus |
| delayShow  | number   | A millisecond delay amount before showing the Popover once triggered.  |  | |

## Usage

```html
<Basic.Popover
  level="warning"
  title="Warning you are deleting entities!"
  value={
    <span>
      Do you really want to delete the selected entries?
    </span>
  }>
  {
    <Basic.Button level="link">YES</Basic.Button>
  }
</Basic.Popover>
```
