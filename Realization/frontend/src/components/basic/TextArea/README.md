# TextArea component

Input component for long text. Extended from AbstractFormComponent.

## Parameters
All parameters from AbstractFormComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| - | :- | :- | :- |
| placeholder  | string   | Short description for input.|  |
| rows  | number   | Number of rows in text area  | 3 |

## Usage

```html
<TextArea
   ref="description"
   label="Popis"
   placeholder="Poznámka k uživateli"
   rows={4}
   validation={Joi.string().max(100)}
 />
```
