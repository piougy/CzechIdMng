# DateValue component

Basic text shortener. Extended from AbstractComponent. You can make your text shorter with chosen lenght, format (path or word) and with defined character for cutting. Final text is extended with 3 dots.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| text | string | Text to be shorten | nan |
| maxLen  | number | Maximal length for text | 20 |
| cutChar  | string | Character according which it will cut text | " " | |
| cutPointEnd  | bool | If you want cut from the end (true) or from the begining (false) | true | |

## Usage

```html
<Basic.HideLongText
text="This is text to be shorten" maxLen={5} cutChar=" " cutPointEnd=true
/>
```
```html
<Basic.HideLongText
text="This/is/text/to/be/shorten" maxLen={10} cutChar=" " cutPointEnd=true
/>
```
