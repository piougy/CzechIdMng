# ScriptArea component

Component for editing code (script). Used mainly for editing Groovy and Javascript. Extended from AbstractFormComponent.

## Parameters
All parameters from AbstractFormComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| helpBlock  | string   | Long description show under this component|  |
| mode  | string   | Type of script. Supported are only 'javascript' and 'groovy'  | 'groovy' |

## Usage

```html
<Basic.ScriptArea
  ref="transformToResourceScript"
  mode="groovy"
  helpBlock={this.i18n('acc:entity.SchemaAttributeHandling.transformToResourceScript.help')}
  label={this.i18n('acc:entity.SchemaAttributeHandling.transformToResourceScript.label')}/>
```
