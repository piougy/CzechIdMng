# SelectBox component

Component for create new options for select box. CreatableSelectBox now support only mutlivalues.

## Requirements

- If you want use check, is necessary implement count endpoint (e.q. ``<server>/api/identities/search/count``).

## Parameters

All parameters from AbstractFormComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| manager  | instanceOf(EntityManager)   | Implementation of manager (extended from EnttityManager) for entity type where we want made count for check if entity exists  |  |
| separator | string | Separator used for copy and paste check and parse values into options | ','  |
| filterColumnName  | string   | Ve kterém sloupci se zkontroluje, zda existuje entita | 'identifiers' |
| existentClass | string   | Css class for existent entities | 'existent' |
| nonExistentClass | string | Css class for non existent entities  | 'non-existent' |
| useCheck  | bool   | Use check if identity exits. Is required add manager  |  false  |


## Usage

### CreatableSelectBox
```html
<Basic.CreatableSelectBox ref="selectComponent"
     label="Creatable select box test"
     manager={identityManager}
     placeholder="Select user ..."
     useCheck={true}/>
```
