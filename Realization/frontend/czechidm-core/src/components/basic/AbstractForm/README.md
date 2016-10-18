# Abstract form
Basic component for every form components. Contains basic methods for cloning children, default transform and setting data, interface for get footer in child.

| Parameter    | Type    | Description                                               | Default     |
| ---          | :---    | :---                                                      | :---        |
| data         | object  | Data (js object) for whole form                           | null        |
| showLoading  | bool    | When showLoading is true, then only loading image is show | false        |
| readOnly     | bool    | When readOnly is true, then are all components read only  | false       |
| disabled     | bool    | When disabled is true, then are all components disabled   | false       |
| rendered     | bool    | When rendered is false, then form will be not rendered    | true        |

## Usage
```javascript
<Basic.AbstractForm ref="form" data={detail.entity} showLoading={_showLoading} className="form-horizontal">
  <Basic.TextField
    ref="position"
    label={this.i18n('entity.IdentityContract.position')}
    rendered={false}/>
  <Basic.SelectBox
    ref="treeTypeId"
    manager={this.treeTypeManager}
    label={this.i18n('entity.IdentityContract.treeType')}
    onChange={this.onChangeTreeType.bind(this)}
    required/>
  <Basic.SelectBox
    ref="workingPosition"
    manager={this.treeNodeManager}
    label={this.i18n('entity.IdentityContract.workingPosition')}
    required
    forceSearchParameters={forceSearchParameters}
    hidden={treeTypeId === null}/>
  <Basic.DateTimePicker
    mode="date"
    ref="validFrom"
    label={this.i18n('label.validFrom')}
    hidden={treeTypeId === null}/>
  <Basic.DateTimePicker
    mode="date"
    ref="validTill"
    label={this.i18n('label.validTill')}
    hidden={treeTypeId === null}/>
  <Basic.SelectBox
    ref="guarantee"
    manager={this.identityManager}
    label={this.i18n('entity.IdentityContract.guarantee')}/>
</Basic.AbstractForm>
```
