# Navigation Component

Renders navigation items as detail tabs. Navigation is built from module descriptors. Tab panel renders child items by parameter **parentId**. Items are automatically propagated from redux context (layout - navigation).

## Parameters

| Parameter | Type | Description | Default  |
| - | :- | :- | :- |
| parentId | string  |  which navigation parent wil be rendered - sub menus to render  |  | |

## Usage

```html
<Advanced.TabPanel parentId="user-profile" params={this.props.params}>
  {this.props.children}
</Advanced.TabPanel>
```
