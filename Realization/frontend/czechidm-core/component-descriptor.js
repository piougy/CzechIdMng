module.exports = {
  'id': 'core',
  'name': 'Core',
  'description': 'Components for Core module',
  'components': [
    {
      'id': 'dynamicRoleTaskDetail',
      'component': require('./src/content/task/identityRole/DynamicTaskRoleDetail')
    },
    {
      'id': 'role-tree-node-task-detail',
      'component': require('./src/content/task/roleTreeNode/AutomaticRoleTaskDetail')
    },
    {
      'id': 'assigned-task-dashboard',
      'type': 'dashboard',
      'span': '8',
      'order': '3',
      'component': require('./src/content/dashboards/AssignedTaskDashboard')
    },
    {
      'id': 'profile-dashboard',
      'type': 'dashboard',
      'span': '4',
      'order': '1',
      'component': require('./src/content/dashboards/ProfileDashboard')
    },
    {
      'id': 'password-change-content',
      'priority': 0,
      'component': require('./src/content/identity/PasswordChangeContent')
    },
    {
      'id': 'identity-info',
      'type': 'entity-info',
      'entityType': ['identity', 'IdmIdentity'],
      'component': require('./src/components/advanced/IdentityInfo/IdentityInfo').default,
      'manager': require('./src/redux').IdentityManager
    },
    {
      'id': 'role-info',
      'type': 'entity-info',
      'entityType': ['role', 'IdmRole'],
      'component': require('./src/components/advanced/RoleInfo/RoleInfo').default,
      'manager': require('./src/redux').RoleManager
    },
    {
      'id': 'role-catalogue-info',
      'type': 'entity-info',
      'entityType': ['roleCatalogue', 'IdmRoleCatalogue', 'ROLE_CATALOGUE'],
      'component': require('./src/components/advanced/RoleCatalogueInfo/RoleCatalogueInfo').default,
      'manager': require('./src/redux').RoleManager
    },
    {
      'id': 'tree-node-info',
      'type': 'entity-info',
      'entityType': ['treeNode', 'IdmTreeNode'],
      'component': require('./src/components/advanced/TreeNodeInfo/TreeNodeInfo').default,
      'manager': require('./src/redux').TreeNodeManager
    },
    {
      'id': 'notification-template-info',
      'type': 'entity-info',
      'entityType': ['notificationTemplate', 'IdmNotificationtemplate'],
      'component': require('./src/components/advanced/NotificationTemplateInfo/NotificationTemplateInfo').default,
      'manager': require('./src/redux').NotificationTemplateManager
    },
    {
      'id': 'tree-type-info',
      'type': 'entity-info',
      'entityType': ['treeType', 'IdmTreeType'],
      'component': require('./src/components/advanced/TreeTypeInfo/TreeTypeInfo').default,
      'manager': require('./src/redux').TreeTypeManager
    },
    {
      'id': 'identity-contract-info',
      'type': 'entity-info',
      'entityType': ['contract', 'identityContract', 'IdmIdentityContract'],
      'component': require('./src/components/advanced/IdentityContractInfo/IdentityContractInfo').default,
      'manager': require('./src/redux').IdentityContractManager
    },
    {
      'id': 'text-form-value',
      'type': 'form-value',
      'persistentType': 'TEXT',
      'faceType': 'TEXT',
      'component': require('./src/components/advanced/Form/TextFormValue'),
      'labelKey': 'core:component.advanced.EavForm.faceType.TEXT'
    },
    {
      'id': 'char-form-value',
      'type': 'form-value',
      'persistentType': 'CHAR',
      'faceType': 'CHAR',
      'component': require('./src/components/advanced/Form/CharFormValue'),
      'labelKey': 'core:component.advanced.EavForm.faceType.CHAR'
    },
    {
      'id': 'int-form-value',
      'type': 'form-value',
      'persistentType': 'INT',
      'faceType': 'INT',
      'component': require('./src/components/advanced/Form/IntFormValue'),
      'labelKey': 'core:component.advanced.EavForm.faceType.INT'
    },
    {
      'id': 'long-form-value',
      'type': 'form-value',
      'persistentType': 'LONG',
      'faceType': 'LONG',
      'component': require('./src/components/advanced/Form/LongFormValue'),
      'labelKey': 'core:component.advanced.EavForm.faceType.LONG'
    },
    {
      'id': 'double-form-value',
      'type': 'form-value',
      'persistentType': 'DOUBLE',
      'faceType': 'DOUBLE',
      'component': require('./src/components/advanced/Form/DoubleFormValue'),
      'labelKey': 'core:component.advanced.EavForm.faceType.DOUBLE'
    },
    {
      'id': 'currency-form-value',
      'type': 'form-value',
      'persistentType': 'DOUBLE',
      'faceType': 'CURRENCY',
      'component': require('./src/components/advanced/Form/CurrencyFormValue'),
      'labelKey': 'core:component.advanced.EavForm.faceType.CURRENCY'
    },
    {
      'id': 'boolean-form-value',
      'type': 'form-value',
      'persistentType': 'BOOLEAN',
      'faceType': 'BOOLEAN',
      'component': require('./src/components/advanced/Form/BooleanFormValue'),
      'labelKey': 'core:component.advanced.EavForm.faceType.BOOLEAN'
    },
    {
      'id': 'date-form-value',
      'type': 'form-value',
      'persistentType': 'DATE',
      'faceType': 'DATE',
      'component': require('./src/components/advanced/Form/DateFormValue'),
      'labelKey': 'core:component.advanced.EavForm.faceType.DATE'
    },
    {
      'id': 'datetime-form-value',
      'type': 'form-value',
      'persistentType': 'DATETIME',
      'faceType': 'DATETIME',
      'component': require('./src/components/advanced/Form/DateTimeFormValue'),
      'labelKey': 'core:component.advanced.EavForm.faceType.DATETIME'
    },
    {
      'id': 'textarea-form-value',
      'type': 'form-value',
      'persistentType': 'TEXT',
      'faceType': 'TEXTAREA',
      'component': require('./src/components/advanced/Form/TextAreaFormValue'),
      'labelKey': 'core:component.advanced.EavForm.faceType.TEXTAREA'
    },
    {
      'id': 'richtextarea-form-value',
      'type': 'form-value',
      'persistentType': 'TEXT',
      'faceType': 'RICHTEXTAREA',
      'component': require('./src/components/advanced/Form/RichTextAreaFormValue'),
      'labelKey': 'core:component.advanced.EavForm.faceType.RICHTEXTAREA'
    },
    {
      'id': 'bytearray-form-value',
      'type': 'form-value',
      'persistentType': 'BYTEARRAY',
      'faceType': 'BYTEARRAY',
      'component': require('./src/components/advanced/Form/ByteArrayFormValue'),
      'labelKey': 'core:component.advanced.EavForm.faceType.BYTEARRAY'
    },
    {
      'id': 'uuid-form-value',
      'type': 'form-value',
      'persistentType': 'UUID',
      'faceType': 'UUID',
      'component': require('./src/components/advanced/Form/UuidFormValue'),
      'labelKey': 'core:component.advanced.EavForm.faceType.UUID'
    },
    {
      'id': 'identity-select-form-value',
      'type': 'form-value',
      'persistentType': 'UUID',
      'faceType': 'IDENTITY-SELECT',
      'component': require('./src/components/advanced/Form/IdentitySelectFormValue'),
      'labelKey': 'core:component.advanced.EavForm.faceType.IDENTITY-SELECT'
    }
  ]
};
