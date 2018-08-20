# ValidationMessage Component

Component show validation message for password validation.

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| error | object | error contains validation error by password change | |
| validationDefinition | object | prevalidaton / password validation - true / false | true |

## Usage
```html
<PasswordPreValidation rendered={validationDefinition && validationError !== undefined} error={validationError} />
```
