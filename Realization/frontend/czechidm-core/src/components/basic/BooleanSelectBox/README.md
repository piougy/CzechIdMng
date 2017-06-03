# BooleanSelectBox component

Component for select boolean values. Extended from EnumSelectBox.

**Warning: String representation for option value is needed - false value not work as selected value for react-select clearable functionality**

## Parameters

All parameters form parent component ``EnumSelectBox`` is supported.
<br><br>Useful parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| options | arrayOf(object) | Boolean values custom localization, see usage | [{ value: 'true', niceLabel: this.i18n('label.yes') },{ value: 'false', niceLabel: this.i18n('label.no') }] |



## Usage
### Without custom options
```html
<Basic.BooleanSelectBox
  ref="select"
  label="Send notification"
/>

```
### With custom options
```html
<Basic.BooleanSelectBox
  ref="select"
  label="Send notification"
  options={ [
    { value: 'true', niceLabel: 'To all identities' },
    { value: 'false', niceLabel: 'To selected identities' }
  ]}
/>

```
