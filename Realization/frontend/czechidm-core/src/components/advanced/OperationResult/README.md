# OperationResult component

Shows font awesome's info icon and in popover there is localized flash message and operation result code.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| value | instanceOf(OperationResult) | externally loaded operation result | null |
| face | oneOf(['full', 'popover'])  |  Decorator: <ul>li>`popover`: result code with result code message</li><li>`full`: full info card with exception</li></ul>  |  full |
| stateLabel | sting | external label for operation state | label by result's OperationState enumeration  |
| detailLink | oneOf([string, func]) | link (string) or function to show result's detail |  |
| header | string| header text | 'result.header' component locale | |

## Usage

```html
<Advanced.OperationResult value={ entity.result } />
```
