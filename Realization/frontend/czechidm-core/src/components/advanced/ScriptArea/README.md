# ScriptArea component

Advanced component for editing code (script). Used mainly for editing Groovy and Javascript. Extended from Basic component ScriptArea.
In this component is overriden options button (add button for add script via ScriptManager).

## Parameters
All parameters from ScriptArea are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| scriptManager  | Manager   |Required props, manager for Scripts |  |
| scriptCategory  | Enum   | Script category enum. Show script only for category  | null |


## Usage

```html
<Advanced.ScriptArea
  ref="transformToResourceScript"
  scriptCategory={Enums.ScriptCategoryEnum.findKeyBySymbol(Enums.ScriptCategoryEnum.TRANSFORM_TO)}
  headerText="Select script"
  label="Script"
  scriptManager={scriptManager} />
```
