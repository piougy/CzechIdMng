# Div Component

Div container (supports rendered and showLoading properties)

## Usage

```html
<Basic.Div rendered={entity.identityRoleAttributeDefinition}>
  <Basic.ContentHeader icon="fa:th-list" text={ this.i18n('content.role.formAttributes.header') } style={{ marginBottom: 0 }}/>
  <RoleFormAttributeTable
    uiKey="role-form-attributes-table"
    forceSearchParameters={ forceSearchParameters }
    className="no-margin"
    params={ this.props.params }/>
</Basic.Div>
```
