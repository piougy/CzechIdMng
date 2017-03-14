# Abstract form component
Parent for every component use in abstract form.

## Usage
This component is not designed for separate usage. Use children components (etc. TextField).

## Common parameters

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| value  | object |  |  |
| label  | string |  |  |
| disabled  | bool |  | false |
| hidden  | bool | adds css hidden | false |
| labelSpan  | string | Defined span for label (usable with horizontal-form css) |  |
| componentSpan  | string | defined span for component (usable with horizontal-form css) |  |
| required  | bool | adds default required validation and asterix | false |
| readOnly  | bool | html readonly | false |
| onChange  | func |  |  |
| validation  | object | joi validation |  |
| validate  | func | function for custom validation (input is value and result from previous validations) |  |
| style  | string | element style |  |
| helpBlock  | string | help under input |  | |
