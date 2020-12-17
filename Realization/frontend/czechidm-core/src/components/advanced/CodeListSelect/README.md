# CodeListSelect component

Component for select item from code list.
Component supported single select and multi select mode.

## Parameters

All parameters form parent component ``AbstractFormComponent`` is supported.
<br><br>Extra component parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| label  | string   | Selectbox label |  |
| placeholder | string | Selectbox placeholder | |
| helpBlock  | string   | Selectbox helpblock | |
| forceSearchParameters | Object   | Hard filter | false |
| useFirst | bool | Use the first searched value, if value is empty | |
| items  | Array of Objects   | Items which will be displayed  |  |
| multiSelect  | bool   | selecting multiple options | false |
| showOnlyIfOptionsExists | bool | Visible only if there is at least one option | false |
| onChange | func | called when option is changed  | |
| searchable | bool   | selectbox is searchable | true |

## Usage

```html
<Advanced.CodeListSelect
    ref="list"
    code="list"
    label={ this.i18n('some.label') }
    multiSelect
    onChange={ this.changeAction.bind(this) }
    searchable={ false }
/>
```
