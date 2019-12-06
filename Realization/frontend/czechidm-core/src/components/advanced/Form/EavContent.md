# EavContent Component

Content with eav form. Could be added to entities, which supports dynamic eav attributes.
Automatically loads ``formInstance`` from BE by conventions (see and generalize``FormableEntityManager``).

## Parameters

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| formableManager  | object.isRequired   | Manager for formInstance loading (generalize ``FormableEntityManager``) |  |
| entityId  | string.isRequired   | Parent entity identifier |  |
| contentKey  | string   | For localization |  |
| showSaveButton  | bool   | Save button will be shown | false |

## Usage

```html
const entityId = 'testUsername';
const uiKey = `eav-identity`;
const manager = new IdentityManager();

<Advanced.EavContent
  formableManager={ manager }
  entityId={ entityId }
  contentKey="content.identity.eav"
  showSaveButton/>
```
