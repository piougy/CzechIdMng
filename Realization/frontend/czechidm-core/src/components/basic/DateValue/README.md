# DateValue component

Basic DateValue date formatter. Extended from AbstractComponent. You can use advanced component DateValue with showTime parameters, please use advanced component.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| rendered | boolean | If component is rendered on page | true |
| value  | string | Date value in iso-8601 format |  |
| format  | string | Format for date time |  | |

## Usage

```html
<DateValue
  value="2016-05-02T00:00:00"
  format="d MMM"
/>
```
