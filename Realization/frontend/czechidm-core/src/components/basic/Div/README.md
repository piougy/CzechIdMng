# Div Component

Div container (supports rendered and showLoading properties).

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| showAnimation | bool    | When loading is active, shows animation too   |   true |

## Usage

```html
<Basic.Div rendered={entity.identityRoleAttributeDefinition}>
  <Basic.ContentHeader icon="fa:th-list" text={ this.i18n('content.role.formAttributes.header') } style={{ marginBottom: 0 }}/>
  <RoleFormAttributeTable
    uiKey="role-form-attributes-table"
    forceSearchParameters={ forceSearchParameters }
    className="no-margin"
    match={ this.props.match }/>
</Basic.Div>
```
