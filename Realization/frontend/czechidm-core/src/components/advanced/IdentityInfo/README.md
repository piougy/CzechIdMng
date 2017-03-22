# IdentityInfo Component

Information about identity. If identity doesn't exist, then short identity's idenfifier will be shown.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| entity | instanceOf(Identity)  |  externally loaded identity. If entity is given, then fetching entity frem BE is not needed.  |  |
| username | string  |  Selected identity's username - identity will be loaded automatically. `entityIdentifier` alias, has lower priority  |  |
| entityIdentifier | string  |  Selected identity's id - identity will be loaded automatically. `username` alias, has higher priority.  |  |
| face | oneOf(['full', 'link', 'text'])  |  Decorator: <ul><li>`text`: entity's nice label only</li><li>`link`: entity's nice label with link to detail</li><li>`full`: full info card</li></ul>  |  full |
| showLink | bool | Shows link to entity's detail | true |


## Usage

```html
<Advanced.IdentityInfo entity={{name: 'login', firstName: 'Jan', lastName: 'Novák'}}/>
```

or

```html
<Advanced.IdentityInfo entityIdentifier="login" face="link" />
```
