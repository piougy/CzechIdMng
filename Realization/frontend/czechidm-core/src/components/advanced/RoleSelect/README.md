# RoleSelect Component

Component contains selectbox with role select + button for show role catalogue as
tree with table select.

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| columns | array of strings | columns that will be show in role table | ['name'] |
| isModal | bool  | component will be show in modal  | false |
| multiSelect | bool | select more than one role | true |
| showActionButtons  | bool   | show select buttons for table | true |
| showBulkAction  | bool   | Bottom of table show buttons with bulk actions. | false |
| selectRowClass | string | Row select class | 'success' |
| onCatalogueShow | func | Callback that will be trigger after click to button for show role table | null  |

## Usage
```html
<Advanced.RoleSelect
  required
  multiSelect
  showActionButtons
  onCatalogueShow={this._showRoleCatalogueTable.bind(this)}
  ref="role"/>
```
