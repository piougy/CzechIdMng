# Icon Component

Shows glyphicon.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| type  | oneOf(['glyph', 'fa', 'component'])   | glyphicon, font-awesome or custom component registred by ``component-descriptor.js`` | 'glyph' |
| icon  | string   | glyphicon, font-awesome suffix name or component identifier. Could contain type definition in format ``fa:group``, ``component:identity`` | if empty, then icon isn't rendered |
| value  | string   |  icon parameter alias  |  |
| iconSize  | oneOf(['default', 'sm', 'lg'])  | Icon size sm = 2x, lg = 6x.  | 'default' |

## Usage

```html
<Icon type="fa" icon="user" showLoading={ false }/>
```
