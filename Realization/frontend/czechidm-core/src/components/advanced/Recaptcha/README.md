# Reacaptcha component

Is extended from AbstractFormComponent. Is required to check before form is sent to BE.

Uses Google ReCaptcha for its protection. This step is not required but is recommended for production use. Otherwise you won't be protected from bots.

- Sign for your ReCaptcha site key. Do it [here](https://www.google.com/recaptcha/admin).
- Update your backend configuration ([IDM]/WEB-INF/classes/application.properties). Add these properties:
  - idm.sec.security.recaptcha.url=https://www.google.com/recaptcha/api/siteverify
  - idm.sec.security.recaptcha.secretKey=[YOUR_SECRET_KEY]
- Update your frontend configuration for given profile and stage [FRONTEND]/czechidm-app/config/[PROFILE]/[STAGE]. Add these properties:
```
"recaptcha": {
    "siteKey": "[YOUR_SITE_KEY]",
    "enabled": true
}
```


## Parameters

Supported parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| label  | string |  |  |
| labelSpan  | string | Defined span for label (usable with horizontal-form css) |  |
| componentSpan  | string | defined span for component (usable with horizontal-form css) |  |
| helpBlock  | string | help under input |  | |

## Usage

```html
<Advanced.Recaptcha
  label="Are you a robot?"
  ref="recaptcha"/>
```
