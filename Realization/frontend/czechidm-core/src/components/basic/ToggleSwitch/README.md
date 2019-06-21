# ToggleSwitch component

Is extended from AbstractFormComponent.

## Parameters

All parameters from AbstractFormComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| labelSpan  | string | Defined span for label | col-sm-offset-3 |
| tooltip  | string | Tooltip show on hover| | |

## Usage

```html
<Basic.ToggleSwitch
  ref="switchShowChangesOnly"
  label={this.i18n('switchShowChangesOnly')}
  onChange={this._toggleShowChangesOnly.bind(this)}
  value={showChangesOnly}
 />
```
