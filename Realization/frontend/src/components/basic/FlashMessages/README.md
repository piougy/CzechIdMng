# FlashMessages Component

User notification system shows messages to end user.
Messages are propagated by redux layer.

## Usage

Component is included in layout (App.js). Its not recommended use more than one FlashMessages component on page.

```html
<FlashMessages ref="messages"/>
```

For message adding can be called function *addMessage* of class AbstractContextComponent (this is useful, when our class is their subclass):

```javascript
...
addMessage(message, event) {
  if (event) {
    event.preventDefault();
  }
  this.context.store.dispatch(addMessage(message));
}
...
```
or redux action addMessage can be called directly:

```javascript
...
this.context.store.dispatch(addMessage({message : 'Identity john.doe was successfully saved.'}));
...
```

## Message parameters

| Parameter | Type | Description | Default  |
| - | :- | :- | :- |
| key  | string:optional   | Just one message with the same key is shown in the same time | "global-success" |
| title  | string:optional   | Strong message heading | "Operation success" |
| message  | string:optional   | Message body | "Identity john.doe was successfully saved." |
| level  | ["success", "info", "warning", "error"]:optional   | Message level / color | "success" |
| position  | ["tr", "tc"]:optional   | Message position. Default "tr" | "tc" for errors and warnings, "tr" others |
| date  | date  | Message date | new Date() |
