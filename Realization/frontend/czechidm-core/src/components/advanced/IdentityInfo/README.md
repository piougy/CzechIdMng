# IdentityInfo Component

Information about identity.

## Parameters

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| identity | instanceOf(Identity)  |  identity  |  |
| username | string  |  Selected identity's username - identity will be loaded automatically  |  |
| id | string  |  Selected identity's id - identity will be loaded automatically. Username alias, has higher priority.  |  |
| face | oneOf(['full', 'link'])  |  Decorator  |  full | 

## Usage

```html
<IdentityInfo identity={{name: 'login', firstName: 'Jan', lastName: 'Novák'}}/>
```

or

```html
<IdentityInfo username="login" face="link" />
```
