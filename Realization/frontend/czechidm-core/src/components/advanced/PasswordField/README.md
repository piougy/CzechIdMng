# PasswordField component

Component for create two text field with new password and new password again. These text field are complemented with strength password estimator.
New password you receive from method 'getNewPassword()', new password you receive from 'getNewPasswordAgain()'.

## Parameters

All parameters from AbstractFormComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| newPassword  | string   | Default value for new password text field | '' |
| newPasswordAgain | string | Defailt value for new password again text field |   |
| readOnly  | bool   | Set if component is in read only mode | false |
| type | string   | Set type of text field component. | 'password' |
| required | bool  | If true bouth text field will be required | true |

## Usage
### Select
```html
<Password
  className="form-control"
  ref="passwords"
  type={generatePassword ? 'text' : 'password'}
  required={!generatePassword}
  readOnly={generatePassword}
  newPassword={password}
  newPasswordAgain={passwordAgain}/>
```
