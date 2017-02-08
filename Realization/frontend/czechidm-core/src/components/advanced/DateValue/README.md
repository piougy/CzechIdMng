# DateValue component

Advanced DateValue date formatter. Extended from AbstractContextComponent, reuses Basic.DateValue component and automatically adds default date/time format loaded from localization.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| rendered | boolean | If component is rendered on page | true |
| value  | string | Date value in iso-8601 format |  |
| showTime | boolean | Show date and time | false |
| format  | string | Format for date time |  | |

## Usage

```html
<Advanced.DateValue
  value="2016-05-02T00:00:00"
  showTime/>
```
