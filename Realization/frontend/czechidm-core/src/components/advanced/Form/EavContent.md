# EavContent Component

Content with eav form. Could be added to entities, which supports dynamic eav attributes.
Automatically loads ``formInstance`` from BE by conventions (see and generalize``FormableEntityManager``).

## Parameters

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| uiKey  | string   | UI identifier - it's used as key in store (saving, loading ...) |  |
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
  uiKey={ uiKey }
  formableManager={ manager }
  entityId={ entityId }
  contentKey="content.identity.eav"
  showSaveButton/>
```
