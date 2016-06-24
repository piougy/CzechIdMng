# EnumLabel component

Non editable component for show localized enum item as label. Extended from AbstractFormComponent.

## Parameters

All parameters from AbstractFormComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| - | :- | :- | :- |
| enum  | object  | Enumeration | - | |


## Usage

```html
  <Basic.EnumLabel
    ref="wfState"
    enum={VpnActivityStateEnum}
    value="IMPLEMENTED"
    label={this.i18n('wfState')}/>
 />
```
