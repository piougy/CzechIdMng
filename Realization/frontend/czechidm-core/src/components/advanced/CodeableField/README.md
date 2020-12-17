# CodeableField component

Component with required ``code`` and ``name`` text fields.
``getValue()`` method returns both properties in object.

## Parameters

Basic parameters form parent component ``AbstractFormComponent`` is supported, but custom ``validation`` an ``onChange`` listener is not provided yet.
<br><br>Extra component parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| codeProperty  | string   | Property for code - returned as object property from ``getValue()`` method. | ``code`` |
| nameProperty | string | Property for code - returned as object property from ``getValue()`` method. | ``name`` |
| codeLabel  | string   | Property code - field label. | ``i18n('entity.code.label')`` |
| nameLabel | string | Property name - field label.  |  ``i18n('entity.name.label')`` |
| codePlaceholder  | string   | Property code - field placeholder. | ``i18n('entity.code.placeholder')`` |
| namePlaceholder | string | Property name - field placeholder. |  |
| codeHelpBlock  | string   | Property code - field helpBlock. |  |
| nameHelpBlock | string | Property name - field helpBlock. |  |
| codeReadOnly  | bool   | Property code - readOnly field. | ``false`` |
| nameReadOnly | bool | Property name - readOnly field. | ``false`` |

## Usage

```javascript
...
const { code, name } = this.refs.codeable.getValue();
...
this.refs.codeable.setValue({ code: 'codeOne', name: 'nameOne' });
...
```

```html
<Advanced.CodeableField
    ref="codeable"
/>
```
