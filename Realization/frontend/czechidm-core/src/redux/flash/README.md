# FlashMessages Actions

User notification system shows messages to end user.

Methods `addErrorMessage` and `addError` automatically handles errors:
* when beackend server is unavailable - message will be shown after 3 seconds
* login errors - makes redirect to login content
* password change is reguired - makes redirect to password change content

## Usage

```javascript
...
this.context.store.dispatch(addMessage({message : 'Identity john.doe was successfully saved.'}));
...
```

```javascript
...
.catch(error => {
  dispatch(addErrorMessage({
    key: 'error-load',
    level: 'error',
    title: 'Nepodařilo se načíst seznam entit'
  }, error));
});
...
```

## Message parameters

@see FlashMessages Component readme.md.
