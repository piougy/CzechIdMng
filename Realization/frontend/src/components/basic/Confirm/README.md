# Confirm dialog component

Confirm dialog as react component.


## Usage

```html
  <Confirm ref="confirm"/>
```

```javascript
this.refs.confirm.show('Are you sure?', 'Title').then(result => {
    // Confirmed
}, (err) => {
    // Rejected
    // must be defined, but can be empty (otherwise uncaught exception to console is shown)
});
```
