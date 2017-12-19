# EnumValue component

Renders enum value as `Basic.Label` with level, icon and localized label by given enum value

## Parameters

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| rendered  | bool  | Enumeration | `true` |
| enum  | func.isRequired  | Enumeration | --- |
| value  | sting  | Enumeration value | --- |
| label  | sting  | Custom label - level will be used by enum value, but label will be this one. If no label is given, then localized label by enum value will be used. | --- | |


## Usage

```html
  <Basic.EnumValue value="EXECUTED" enum={ Enums.OperationStateEnum } />
 />
```
