# PasswordPreValidation Component

Component show validation message for password validation in popup component.

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| error | object | error contains validation error by password change | |
| validationDefinition | object | prevalidaton / password validation - true / false | true |

## Usage
```html
<PasswordPreValidation rendered={validationDefinition && validationError !== undefined} error={validationError} />
```
