# SelectBox component

Component for search and select item any entity. Extended from AbstractFormComponent.
Component supported single select and multi select mode.

## Parameters

All parameters from AbstractFormComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| - | :- | :- | :- |
| service  | instanceOf(AbstractService)   | Implementation of service (extended from AbstractService) for entity type where we want search |  |
| searchInFields  | arrayOf(string)   | Fields in which we want search|  |
| fieldLabel  | string   | Field use for show string representation of item in select box| 'niceLabel' this is automatic added field from service for item |
| multiSelect | bool   | If is true then component is in multi select mode| false |
| value | string or Array of strings (object or Array of objects)  | Value can contains object (object type have to equals with service entity type) or id of entity in string. In multi select mod can be in value Array (string or object) | |
| placeholder  | string   | Short description for input  |  | |


## Usage
### Select
```html
<SelectBox ref="selectComponent"
     label="Select box test"
     service={identityService}
     searchInFields={['lastName', 'name']}
     placeholder="Select user ..."
     value="admin"
     validation={Joi.object().required()}/>
```

### Multi select

```html
<SelectBox
  ref="selectBoxMulti"
  label="Select box multi"
  service={identityService}
  value = {['admin','testCreate11','testCreate2','testCreate3','testCreate4','testCreate5','testCreate6']}
  searchInFields={['lastName', 'name','email']}
  placeholder="Vyberte uživatele ..."
  multiSelect={true}
  required/>
```
