# EntityInfo Component

Information about entity. Entity is defined by `entityType` and `entityIdentifier`. By `entityType` is loaded appropriate entity info component (generalize `AbstractEntityInfo` component) from component descriptor (component.xml). Component has to have `entity-info` component type. Example:

```javascript
{
  'id': 'identity-info', // unique component id
  'type': 'entity-info', // component type
  'entityType': ['identity', 'IdmIdentity'], // this component could render entity types
  'component': require('./src/components/advanced/IdentityInfo/IdentityInfo').default, // target component for entity rendering
  'manager': require('./src/redux').IdentityManager // entity manager - will be used for nice label (only for now - is not required, if EntityInfo.getNiceLabel is not needed - see example above)
},
```

All plugable entity info components (generalize `AbstractEntityInfo` component) should implement:
- all parameters from AbstractComponent
- fetching entity by identifier from BE
- If entity doesn't exist, then short entity's idenfifier will be shown. Use `UuidInfo` component.
- different decorators - see `face` parameter
- `showLink` parameter

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| entity | object  |  Selected entity - externally loaded.  Has higher priority, when is given, then loading is not needed. |  |
| entityType | string.isRequired  |  |  |
| entityIdentifier | string  |  Selected entity's identifier - entity will be loaded automatically.  |  |
| face | oneOf(['full', 'popover', 'link', 'text'])  |  Decorator: <ul><li>`text`: entity's nice label only</li><li>`link`: entity's nice label with link to detail</li>li>`popover`: entity's nice label with link to popover with full info card</li><li>`full`: full info card</li></ul>  |  full |
| showLink | bool | Shows link to entity's detail | true |
| showEntityType | bool | Shows entity type, when no entity info component is found. Set to `false` when type is rendered extrnally (e.g. in different table column) | true |


## Usage

```html
<Advanced.EntityInfo entityType="identity" entity={{name: 'login', firstName: 'Jan', lastName: 'Novák'}}/>
```

or

```html
<Advanced.EntityInfo entityType="identity" entityIdentifier="test" face="link" />
```

or textual nice label could be needed - returns entity's nice label. Useful for localization params etc.

```js
const niceLabel = Advanced.EntityInfo.getNiceLabel('identity', { username: 'test', lastName: 'Test'});
```
