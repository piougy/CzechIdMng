# Modal Component

Wrapped react bootstrap modal
* adds backdrop fix
* https://github.com/react-bootstrap/react-bootstrap/blob/v0.28.3/src/Modal.js
* https://react-bootstrap.github.io/components.html#modals


## Usage

```html
<Basic.Modal show={true} onHide={() => {alert('onHide')}} bsSize="large">
  <Basic.Modal.Header text="Title" closeButton/>
  <Basic.Modal.Body>
    Body ...
  </Basic.Modal.Body>
  <Basic.Modal.Footer>
    Footer
  </Basic.Modal.Footer>
</Basic.Modal>

```
