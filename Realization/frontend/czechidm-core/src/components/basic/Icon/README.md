# Icon Component

Shows glyphicon.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| type  | oneOf(['glyph', 'fa'])   | glyphicon or font-awesome | 'glyph' |
| icon  | string   | glyphicon or font-awesome suffix name. Could contain type definition in format `fa:group` | if empty, then icon isn't rendered |
| value  | string   |  icon parameter alias  |  ||

## Usage

```html
<Icon type="fa" icon="user" showLoading={false}/>
```
