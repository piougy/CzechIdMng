# Link Component

Render basic html "a" link with icon. Can be used for external link primarily.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| href  |  string.isRequired | Standard "href" link parameter |  |
| isExternal  | bool   | External link is opened in new window (=> target="_blank") | true |
| text  | string   |  Link text (or children can be used, see usage) |  ||

## Usage

```html
<Basic.Link href="http://blog.bcvsolutions.eu/" text="Blog"/>
```

```html
<Basic.Link href="http://blog.bcvsolutions.eu/">
  Blog
</Basic.Link>
```
