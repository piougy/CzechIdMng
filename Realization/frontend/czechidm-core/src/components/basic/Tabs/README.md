# Tabs Component

Wrapped react bootstrap Tabs and Tab
* https://react-bootstrap.github.io/components.html#tabs


## Usage

```html
<Basic.Tabs >
  <Basic.Tab eventKey={1} title="Tab One">
    content 1
  </Basic.Tab>
  <Basic.Tab eventKey={2} title="Tab Two">
    content 2
  </Basic.Tab>
  <Basic.Tab eventKey={3} title="Tab Three">
    content 3
  </Basic.Tab>
</Basic.Tabs>
```
## Usage
#### When we want control select tab programmatically:
```html
<Basic.Tabs onSelect={onSelect} activeKey={activeKey} >
  <Basic.Tab eventKey={1} title="Tab One">
    content 1
  </Basic.Tab>
  <Basic.Tab eventKey={2} title="Tab Two">
    content 2
  </Basic.Tab>
  <Basic.Tab eventKey={3} title="Tab Three">
    content 3
  </Basic.Tab>
</Basic.Tabs>
```
