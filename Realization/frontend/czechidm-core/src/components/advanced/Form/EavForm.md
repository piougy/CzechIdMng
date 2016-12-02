# EavForm Component

Dynamic form renders form instance domain object (form definition + form values). For definition contains form attributes with certain persistent type. This persistent type is used for render specific form component (Checkbox, TextField ..)

## Usage
```html
...
import * as Advanced from '../../components/advanced';
...
<Advanced.EavForm ref="eav" formInstance={formInstance}/>
```
