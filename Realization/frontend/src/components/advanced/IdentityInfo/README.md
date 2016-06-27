# IdentityInfo Component

Information about identity.

## Parameters

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| identity | instanceOf(Identity)  |  identity  |  |
| username | string  |  Selected identity's username - identity will be loaded automatically  |  | |

## Usage

```html
<IdentityInfo identity={{name: 'login', firstName: 'Jan', lastName: 'Novák'}}/>
```

or

```html
<IdentityInfo username="login" />
```
