# PasswordChangeComponent Component

Component contains text field for old password and two fields for check new password.
Component check permission for password change and administration permission.

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| requireOldPassword | bool | columns that will be show in role table | true |
| userContext | object  | user context  | null |
| accountOptions | array of strings | All account for thah will be password changed | null |
| entityId | uuid | Id of entity for that will be password changed |  | |

## Usage
```html
<Advanced.PasswordChangeComponent
  ref="passwordComponent"
  userContext={userContext}
  accountOptions=[{ value: 'CzechIdM', niceLabel: 'CzechIdM'}]
  entityId={entityId}/>
```
