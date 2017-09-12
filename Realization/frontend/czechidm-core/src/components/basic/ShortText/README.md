# ShortText component

Basic text shortener. Extended from AbstractComponent. You can make your text shorter with chosen length, format (path or word) and with defined character for cutting. Final text is extended with 3 dots.

## Parameters

All parameters from AbstractComponent are supported except showloading. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| text | string | Text to be shorten |  |
| value | string | Text to be shorten (text alias - text has higher priority) |  |
| maxLength  | number | Maximal length for text | 20 |
| cutChar  | string | Character according which it will cut text | " " | |
| cutPointEnd  | bool | If you want cut from the end (true) or from the beginning (false) | true | |

## Usage

```html
<Basic.ShortText
  text="This is text to be shorten"
  maxLength={ 5 }
  cutChar=" "
  cutPointEnd
/>
```
```html
<Basic.ShortText
  text="This/is/text/to/be/shorten"
  maxLength={ 10 }
  cutChar=" "
  cutPointEnd
/>
```
