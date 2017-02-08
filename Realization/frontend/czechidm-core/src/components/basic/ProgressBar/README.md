# ProgressBar Component

Wrapped react bootstrap ProgressBar
* https://react-bootstrap.github.io/components.html#progress

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| min | number | Start count | 0 |
| max | number.isRequired | End count |  |
| now | number | Actual counter | 0 |
| label | oneOfType([string, bool])] | Label |  |
| active | bool | Adds animation -  the stripes right to left. Not available in IE9 and below. | true |

## Usage

```html
<Basic.ProgressBar min={0} max={4} now={2} label="Zpracováno 2 / 4" active/>
```
