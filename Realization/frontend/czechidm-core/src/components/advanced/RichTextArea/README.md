# RichTextArea component

Input component for WYSIWYG text. Extended from AbstractFormComponent.
All configurable parameters/props can be found on https://jpuri.github.io/react-draft-wysiwyg/#/docs
Component react-draft-wysiwyg extended from draft-js.

## Parameters

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
|  showToolbar | bool | Toolbar is toggled, otherwise is show only when component has focus | false |
|  toolbarOptions | array | Options for toolbar defined which buttons will be available in toolbar, all this options can be found in component props in comment. | 'inline', 'blockType', 'list', 'emoji', 'remove', 'history', 'link' |
|  fontSizeOptions | object | Object with font options that define which options is available for fonts (size, className, dropdownClassName) | { options: [7, 8, 9, 10, 11, 12, 14, 18, 24, 30, 36, 48, 60, 72, 96] } |
|  fontFamilyOptions | object | Object that defined font family options (options, className, dropdownClassName) |{ options: ['Arial', 'Georgia', 'Impact', 'Tahoma', 'Times New Roman', 'Verdana'], |
|  labelSpan | string | Class that define label span |  |
|  label | string | Defined span for label |  |
|  componentSpan | string | Defined span for component |  |
|  placeholder | string | Placeholder nothing else |  |
|  style | style/string | Style that defined inner component draft-js | dfdf |
|  required | bool | adds default required validation and asterix | false |
|  mentions | object | Object that defined hash tag settings  |  |
|  helpBlock | string | help under input  |  |

### Option: mentions
```html
components = {
  separator: ' ',
  trigger: '#',
  caseSensitive: true,
  mentionClassName: 'mention-className',
  dropdownClassName: 'dropdown-className',
  optionClassName: 'option-className',
  suggestions: ['hashTag', 'banana', 'apple']
}
```

## Usage

```html
<RichTextArea ref="textBody" label="Notification body" }
  showToolbar
  helpBlock="Create body for message"}
  mentions={
    separator: ' ',
    trigger: '#',
    caseSensitive: true,
    mentionClassName: 'mention-className',
    dropdownClassName: 'dropdown-className',
    optionClassName: 'option-className',
    suggestions: ['hashTag', 'banana', 'apple']}
    />

```
