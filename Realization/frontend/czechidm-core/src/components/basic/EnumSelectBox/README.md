# EnumSelectBox component

Component for select item any enumeration. Extended from SelectBox.
Component supported single select and multi select mode.

## Parameters

All parameters form parent component ``AbstractFormComponent`` is supported.
<br><br>Extra component parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| enum  | Array of symbols   | Array of symbols and niceLabel function|  |
| options | Array of custom objects (each item must have value and niceLabel field ) | If you want use custom data, then you can use options field | |
| fieldLabel  | string   | Field use for show string representation of item in select box| ``niceLabel`` this is automatic added field from service for item |
| multiSelect | bool   | If is true then component is in multi select mode| false |
| value | symbol or Array of symbols | Value can contains symbol (have to part of enum). In multi select mod can be in value array symbols | |
| placeholder  | string   | Short description for input  |  |
| searchable  | bool   | whether to enable searching feature or not | false |
| useSymbol | bool | returns ``Symbol`` from selected enumeration option. Otherwise returns symbol's key only (string). | true |
| useObject | bool | Return whole object (option)  | false |
| clearable | bool   | Selected options can be cleared| true |



## Usage
### Enumeration select
```html
<EnumSelectBox
  ref="enumSelectBox"
  label="Enum select"
  placeholder="Vyberte enumeraci ..."
  multiSelect={false}
  value={ApiOperationTypeEnum.DELETE}
  enum={ApiOperationTypeEnum}
/>

```

### Multi enumerations select

```html
<EnumSelectBox
  ref="enumSelectBoxMulti"
  label="Enum select multi"
  placeholder="Vyberte enumeraci ..."
  multiSelect={true}
  value={[ApiOperationTypeEnum.DELETE, ApiOperationTypeEnum.CREATE,ApiOperationTypeEnum.UPDATE,ApiOperationTypeEnum.GET]}
  enum={ApiOperationTypeEnum}
  required/>
```

### Multi select for custom data

```html
<EnumSelectBox
  ref="anySelectBoxMulti"
  label="Any select multi"
  multiSelect={true}
  value={['item1','item2']}
  options={[{value: 'item1', niceLabel: 'NiceItem1'},{value: 'item2', niceLabel: 'NiceItem2'}, {value: 'item3', niceLabel: 'NiceItem3'}]}
  required/>
```
### Single select for custom data

```html
<EnumSelectBox
  ref="anySelectBoxMulti"
  label="Any select multi"
  multiSelect={false}
  value="item1"
  options={[{value: 'item1', niceLabel: 'NiceItem1'},{value: 'item2', niceLabel: 'NiceItem2'}, {value: 'item3', niceLabel: 'NiceItem3'}]}
  required/>
```
