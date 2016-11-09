# TextField component

Input text component. Extended from AbstractFormComponent.

## Parameters

All parameters from AbstractFormComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| type  | string   | html input type | text |
| placeholder  | string   | Short description for input.|  | |
| min  | number   | Minimal number string characters for intput |  |
| max  | number   | Maximal number string characters for input  |  |

## Usage

```html
<TextField ref="email"
           label="Email"
           placeholder="User email"
           hidden={false}
           validation={Joi.string().email()}
           min={2}
           max={100}
 />
```
