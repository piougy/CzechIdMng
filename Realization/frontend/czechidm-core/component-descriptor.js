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
      'faceType': 'TEXT',
      'component': require('./src/components/advanced/Form/TextFormValue')
    },
    {
      'id': 'char-form-value',
      'type': 'form-value',
      'faceType': 'CHAR',
      'component': require('./src/components/advanced/Form/CharFormValue')
    },
    {
      'id': 'int-form-value',
      'type': 'form-value',
      'faceType': 'INT',
      'component': require('./src/components/advanced/Form/IntFormValue')
    },
    {
      'id': 'long-form-value',
      'type': 'form-value',
      'faceType': 'LONG',
      'component': require('./src/components/advanced/Form/LongFormValue')
    },
    {
      'id': 'double-form-value',
      'type': 'form-value',
      'faceType': 'DOUBLE',
      'component': require('./src/components/advanced/Form/DoubleFormValue')
    },
    {
      'id': 'currency-form-value',
      'type': 'form-value',
      'faceType': 'CURRENCY',
      'component': require('./src/components/advanced/Form/CurrencyFormValue')
    },
    {
      'id': 'boolean-form-value',
      'type': 'form-value',
      'faceType': 'BOOLEAN',
      'component': require('./src/components/advanced/Form/BooleanFormValue')
    },
    {
      'id': 'date-form-value',
      'type': 'form-value',
      'faceType': 'DATE',
      'component': require('./src/components/advanced/Form/DateFormValue')
    },
    {
      'id': 'datetime-form-value',
      'type': 'form-value',
      'faceType': 'DATETIME',
      'component': require('./src/components/advanced/Form/DateTimeFormValue')
    },
    {
      'id': 'textarea-form-value',
      'type': 'form-value',
      'faceType': 'TEXTAREA',
      'component': require('./src/components/advanced/Form/TextAreaFormValue')
    },
    {
      'id': 'richtextarea-form-value',
      'type': 'form-value',
      'faceType': 'RICHTEXTAREA',
      'component': require('./src/components/advanced/Form/RichTextAreaFormValue')
    },
    {
      'id': 'bytearray-form-value',
      'type': 'form-value',
      'faceType': 'BYTEARRAY',
      'component': require('./src/components/advanced/Form/ByteArrayFormValue')
    },
    {
      'id': 'uuid-form-value',
      'type': 'form-value',
      'faceType': 'UUID',
      'component': require('./src/components/advanced/Form/UuidFormValue')
    },
    {
      'id': 'identity-select-form-value',
      'type': 'form-value',
      'faceType': 'IDENTITY-SELECT',
      'component': require('./src/components/advanced/Form/IdentitySelectFormValue')
    }
  ]
};
