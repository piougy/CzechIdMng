# OperationResult component

Shows font awesome's info icon and in popover there is localized flash message and operation result code.

## Parameters

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| rendered  | bool |  | true |
| result | instanceOf(OperationResult) | externally loaded operation result | null |
| enumLabel | sting | externally loaded value to EnumValue component | null |

## Usage

```html
<Advanced.OperationResult result={ entity.result } enumLabel={ entity.resultState }/>
```
