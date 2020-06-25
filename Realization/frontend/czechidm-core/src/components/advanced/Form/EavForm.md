# EavForm Component

Dynamic form renders form instance domain object (form definition + form values). For definition contains form attributes with certain persistent type. This persistent type is used for render specific form component (Checkbox, TextField ..)


## Parameters

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| formInstance  | string   | Prepared / loaded form instance with form definition and values |  |
| readOnly | bool | Read only form | false |
| useDefaultValue | bool | Use configured attribute default values as filled. | false |
| condensed | bool | Condensed (shorten) form properties - usable in tables. Just filled values without help will be shown. | false |

## Usage
```html
...
import * as Advanced from '../../components/advanced';
...
<Advanced.EavForm ref="eav" formInstance={formInstance}/>
```
