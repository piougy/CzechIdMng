# EntitySelectBox component

Component for search (autocomplete) and select item any entity type. Extended from AbstractFormComponent.
Component supported single select and multi select mode.
Inner component is descibe by component-descriptor.js (see component with type 'entity-select-box')

## Requirements

- If inner component is simple selectBox it is neccessary to pass all requirements from select box component,
- If in component descriptor defined another component you must pass all requirements from this component.

## Parameters

All parameters from SelectBox component are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| entityType  | string  | Type of component see component-descriptor.js for all supported types  |  |
| showDefaultHelpBlock | boolean | If true add to help Block information about field in which will be search. Component must support helpBlock. | false  |



## Usage

### EntitySelectBox
```html
<EntitySelectBox
  ref="someIdentitySelect"}
  multiSelect
  entityType="identity"
  showDefaultHelpBlock/>
```
