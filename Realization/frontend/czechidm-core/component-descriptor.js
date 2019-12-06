module.exports = {
  id: 'core',
  name: 'Core',
  description: 'Components for Core module',
  components: [
    {
      id: 'dynamicRoleTaskDetail',
      component: require('./src/content/task/identityRole/DynamicTaskRoleDetail')
    },
    {
      id: 'dynamicTaskRoleConceptDetail',
      component: require('./src/content/task/identityRole/DynamicTaskRoleConceptDetail')
    },
    {
      id: 'dynamicAutomaticRoleTaskDetail',
      component: require('./src/content/task/identityRole/DynamicTaskAutomaticRoleDetail')
    },
    {
      id: 'dynamicRequestTaskDetail',
      component: require('./src/content/task/request/DynamicRequestTaskDetail')
    },
    {
      id: 'role-tree-node-task-detail',
      component: require('./src/content/task/roleTreeNode/AutomaticRoleTaskDetail')
    },
    {
      id: 'identity-role-dashboard',
      type: 'identity-dashboard',
      dashboard: false,
      order: 100,
      component: require('./src/content/dashboards/IdentityRoleDashboard')
    },
    {
      id: 'identity-contract-dashboard',
      type: 'identity-dashboard',
      dashboard: false,
      order: 200,
      component: require('./src/content/dashboards/IdentityContractDashboard')
    },
    {
      id: 'assigned-task-dashboard',
      type: 'identity-dashboard',
      order: 300,
      component: require('./src/content/dashboards/AssignedTaskDashboard')
    },
    {
      id: 'role-request-dashboard',
      type: 'identity-dashboard',
      order: 350,
      component: require('./src/content/dashboards/RoleRequestDashboard')
    },
    {
      id: 'long-running-task-dashboard',
      type: 'identity-dashboard',
      order: 500,
      component: require('./src/content/dashboards/LongRunningTaskDashboard')
    },
    {
      id: 'detail-identity-dashboard-button',
      type: 'identity-dashboard-button',
      order: 100,
      component: require('./src/content/dashboards/button/IdentityDetailDashboardButton')
    },
    {
      id: 'password-change-identity-dashboard-button',
      type: 'identity-dashboard-button',
      order: 200,
      component: require('./src/content/dashboards/button/PasswordChangeDashboardButton')
    },
    {
      id: 'change-permission-identity-dashboard-button',
      type: 'identity-dashboard-button',
      order: 300,
      component: require('./src/content/dashboards/button/ChangePermissionDashboardButton')
    },
    {
      id: 'disable-identity-dashboard-button',
      type: 'identity-dashboard-button',
      order: 400,
      component: require('./src/content/dashboards/button/DisableIdentityDashboardButton')
    },
    {
      id: 'enable-identity-dashboard-button',
      type: 'identity-dashboard-button',
      order: 400,
      component: require('./src/content/dashboards/button/EnableIdentityDashboardButton')
    },
    {
      id: 'password-change-content',
      priority: 0,
      component: require('./src/content/identity/PasswordChangeContent')
    },
    {
      id: 'identity-info',
      type: 'entity-info',
      entityType: ['identity', 'IdmIdentity', 'IdmIdentityDto'],
      component: require('./src/components/advanced/IdentityInfo/IdentityInfo').default,
      manager: require('./src/redux').IdentityManager
    },
    {
      id: 'role-info',
      type: 'entity-info',
      entityType: ['role', 'IdmRole', 'IdmRoleDto'],
      component: require('./src/components/advanced/RoleInfo/RoleInfo').default,
      manager: require('./src/redux').RoleManager
    },
    {
      id: 'role-catalogue-info',
      type: 'entity-info',
      entityType: ['roleCatalogue', 'IdmRoleCatalogue', 'IdmRoleCatalogueDto', 'ROLE_CATALOGUE'],
      component: require('./src/components/advanced/RoleCatalogueInfo/RoleCatalogueInfo').default,
      manager: require('./src/redux').RoleManager
    },
    {
      id: 'tree-node-info',
      type: 'entity-info',
      entityType: ['tree', 'treeNode', 'IdmTreeNode', 'IdmTreeNodeDto'],
      component: require('./src/components/advanced/TreeNodeInfo/TreeNodeInfo').default,
      manager: require('./src/redux').TreeNodeManager
    },
    {
      id: 'notification-template-info',
      type: 'entity-info',
      entityType: ['notificationTemplate', 'IdmNotificationtemplate', 'IdmNotificationtemplateDto'],
      component: require('./src/components/advanced/NotificationTemplateInfo/NotificationTemplateInfo').default,
      manager: require('./src/redux').NotificationTemplateManager
    },
    {
      id: 'tree-type-info',
      type: 'entity-info',
      entityType: ['treeType', 'IdmTreeType', 'IdmTreeTypeDto'],
      component: require('./src/components/advanced/TreeTypeInfo/TreeTypeInfo').default,
      manager: require('./src/redux').TreeTypeManager
    },
    {
      id: 'identity-contract-info',
      type: 'entity-info',
      entityType: ['contract', 'identityContract', 'IdmIdentityContract', 'IdmIdentityContractDto'],
      component: require('./src/components/advanced/IdentityContractInfo/IdentityContractInfo').default,
      manager: require('./src/redux').IdentityContractManager
    },
    {
      id: 'contract-position-info',
      type: 'entity-info',
      entityType: ['contractPosition', 'IdmContractPosition', 'IdmContractPositionDto'],
      component: require('./src/components/advanced/ContractPositionInfo/ContractPositionInfo').default,
      manager: require('./src/redux').ContractPositionManager
    },
    {
      id: 'identity-role-info',
      type: 'entity-info',
      entityType: ['identityRole', 'IdmIdentityRole', 'IdmIdentityRoleDto'],
      component: require('./src/components/advanced/IdentityRoleInfo/IdentityRoleInfo').default,
      manager: require('./src/redux').IdentityRoleManager
    },
    {
      id: 'contract-slice-info',
      type: 'entity-info',
      entityType: ['contractSlice', 'contractSlice', 'IdmContractSlice', 'IdmContractSliceDto'],
      component: require('./src/components/advanced/ContractSliceInfo/ContractSliceInfo').default,
      manager: require('./src/redux').ContractSliceManager
    },
    {
      id: 'text-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'TEXT',
      faceType: 'TEXT',
      component: require('./src/components/advanced/Form/TextFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.TEXT'
    },
    {
      id: 'short-text-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'SHORTTEXT',
      faceType: 'SHORTTEXT',
      component: require('./src/components/advanced/Form/ShortTextFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.SHORTTEXT'
    },
    {
      id: 'char-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'CHAR',
      faceType: 'CHAR',
      component: require('./src/components/advanced/Form/CharFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.CHAR'
    },
    {
      id: 'int-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'INT',
      faceType: 'INT',
      component: require('./src/components/advanced/Form/IntFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.INT'
    },
    {
      id: 'long-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'LONG',
      faceType: 'LONG',
      component: require('./src/components/advanced/Form/LongFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.LONG'
    },
    {
      id: 'double-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'DOUBLE',
      faceType: 'DOUBLE',
      component: require('./src/components/advanced/Form/DoubleFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.DOUBLE'
    },
    {
      id: 'currency-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'DOUBLE',
      faceType: 'CURRENCY',
      component: require('./src/components/advanced/Form/CurrencyFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.CURRENCY'
    },
    {
      id: 'boolean-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'BOOLEAN',
      faceType: 'BOOLEAN',
      component: require('./src/components/advanced/Form/BooleanFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.BOOLEAN'
    },
    {
      id: 'date-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'DATE',
      faceType: 'DATE',
      component: require('./src/components/advanced/Form/DateFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.DATE'
    },
    {
      id: 'datetime-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'DATETIME',
      faceType: 'DATETIME',
      component: require('./src/components/advanced/Form/DateTimeFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.DATETIME'
    },
    {
      id: 'textarea-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'TEXT',
      faceType: 'TEXTAREA',
      component: require('./src/components/advanced/Form/TextAreaFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.TEXTAREA'
    },
    {
      id: 'richtextarea-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'TEXT',
      faceType: 'RICHTEXTAREA',
      component: require('./src/components/advanced/Form/RichTextAreaFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.RICHTEXTAREA'
    },
    {
      id: 'bytearray-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'BYTEARRAY',
      faceType: 'BYTEARRAY',
      component: require('./src/components/advanced/Form/ByteArrayFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.BYTEARRAY'
    },
    {
      id: 'uuid-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'UUID',
      faceType: 'UUID',
      component: require('./src/components/advanced/Form/UuidFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.UUID'
    },
    {
      id: 'identity-select-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'UUID',
      faceType: 'IDENTITY-SELECT',
      component: require('./src/components/advanced/Form/SelectBoxFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.IDENTITY-SELECT',
      manager: require('./src/redux').IdentityManager
    },
    {
      id: 'role-select-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'UUID',
      faceType: 'ROLE-SELECT',
      component: require('./src/components/advanced/Form/RoleSelectFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.ROLE-SELECT',
      manager: require('./src/redux').RoleManager
    },
    {
      id: 'role-can-be-requested-select-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'UUID',
      faceType: 'ROLE-CAN-BE-REQUESTED-SELECT',
      component: require('./src/components/advanced/Form/RoleSelectFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.ROLE-SELECT',
      manager: require('./src/redux').RoleManager,
      searchName: 'can-be-requested'
    },
    {
      id: 'form-definition-select-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'UUID',
      faceType: 'FORM-DEFINITION-SELECT',
      component: require('./src/components/advanced/Form/SelectBoxFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.FORM-DEFINITION-SELECT',
      manager: require('./src/redux').FormDefinitionManager
    },
    {
      id: 'tree-node-select-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'UUID',
      faceType: 'TREE-NODE-SELECT',
      component: require('./src/components/advanced/Form/TreeNodeSelectFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.TREE-NODE-SELECT',
      manager: require('./src/redux').TreeNodeManager
    },
    {
      id: 'role-catalogue-select-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'UUID',
      faceType: 'ROLE-CATALOGUE-SELECT',
      component: require('./src/components/advanced/Form/RoleCatalogueSelectFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.ROLE-CATALOGUE-SELECT',
      manager: require('./src/redux').RoleCatalogueManager
    },
    {
      id: 'boolean-select-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'BOOLEAN',
      faceType: 'BOOLEAN-SELECT',
      component: require('./src/components/advanced/Form/BooleanSelectFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.BOOLEAN-SELECT'
    },
    {
      id: 'attachment-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'ATTACHMENT',
      faceType: 'ATTACHMENT',
      component: require('./src/components/advanced/Form/AttachmentFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.ATTACHMENT'
    },
    {
      id: 'code-list-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'CODELIST',
      faceType: undefined,
      component: require('./src/components/advanced/Form/CodeListSelectFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.CODELIST'
    },
    {
      id: 'identity-select-box',
      type: 'entity-select-box',
      priority: 0,
      localizationKey: 'entity.Identity',
      entityType: ['identity'],
      searchInFields: ['username', 'firstName', 'lastName', 'email', 'description'],
      manager: require('./src/redux').IdentityManager
    },
    {
      id: 'identity-contract-select-box',
      type: 'entity-select-box',
      priority: 0,
      localizationKey: 'entity.IdentityContract',
      entityType: ['contract', 'identityContract'],
      searchInFields: ['position'],
      manager: require('./src/redux').IdentityContractManager
    },
    {
      id: 'role-select-box',
      type: 'entity-select-box',
      priority: 0,
      localizationKey: 'entity.Role',
      entityType: ['role'],
      searchInFields: ['name', 'description'],
      manager: require('./src/redux').RoleManager
    },
    {
      id: 'tree-type-select-box',
      type: 'entity-select-box',
      priority: 0,
      localizationKey: 'entity.TreeType',
      entityType: ['treeType'],
      searchInFields: ['code', 'name'],
      manager: require('./src/redux').TreeTypeManager
    },
    {
      id: 'tree-node-select-box',
      type: 'entity-select-box',
      priority: 0,
      localizationKey: 'entity.TreeNode',
      entityType: ['treeNode'],
      searchInFields: ['code', 'name'],
      manager: require('./src/redux').TreeNodeManager
    },
    {
      id: 'role-catalogue-select-box',
      type: 'entity-select-box',
      priority: 0,
      localizationKey: 'entity.RoleCatalogue',
      entityType: ['roleCatalogue'],
      searchInFields: ['code', 'name'],
      manager: require('./src/redux').RoleCatalogueManager
    },
    {
      id: 'basic-password-change',
      type: 'password-change-component',
      component: require('./src/components/advanced/PasswordChangeComponent/PasswordChangeComponent'),
      disabled: false,
      order: 0,
      col: 6
    },
    {
      id: 'business-role-icon',
      type: 'icon',
      entityType: ['business-role', 'business-roles'],
      component: require('./src/components/advanced/Icon/BusinessRoleIcon')
    },
    {
      id: 'role-icon',
      type: 'icon',
      entityType: ['role', 'roles', 'identity-role', 'identity-roles', 'role-request', 'role-requests', 'request-roles', 'request-role'],
      component: 'fa:key'
    },
    {
      id: 'automatic-role-icon',
      type: 'icon',
      entityType: ['automatic-role', 'automatic-roles', 'automatic-role-request', 'automatic-role-requests'],
      component: 'fa:magic'
    },
    {
      id: 'sub-role-icon',
      type: 'icon',
      entityType: ['sub-role', 'sub-roles'],
      component: 'fa:arrow-down'
    },
    {
      id: 'superior-role-icon',
      type: 'icon',
      entityType: ['superior-role', 'superior-roles'],
      component: 'fa:arrow-up'
    },
    {
      id: 'contract-icon',
      type: 'icon',
      entityType: ['contract', 'contracts', 'contract-position', 'contract-positions', 'contract-slice', 'contract-slices'],
      component: 'fa:building'
    },
    {
      id: 'main-contract-icon',
      type: 'icon',
      entityType: ['main-contract'],
      component: require('./src/components/advanced/Icon/MainContractIcon')
    },
    {
      id: 'script-icon',
      type: 'icon',
      entityType: ['script', 'scripts'],
      component: 'fa:clone'
    },
    {
      id: 'identity-icon',
      type: 'icon',
      entityType: ['identity'],
      component: require('./src/components/advanced/Icon/IdentityIcon')
    },
    {
      id: 'identities-icon',
      type: 'icon',
      entityType: ['identities'],
      component: 'fa:group'
    },
    {
      id: 'enabled-identity-icon',
      type: 'icon',
      entityType: ['enabled-identity'],
      component: 'fa:user'
    },
    {
      id: 'disabled-identity-icon',
      type: 'icon',
      entityType: ['disabled-identity'],
      component: 'fa:user-slash'
    },
    {
      id: 'scheduled-task-icon',
      type: 'icon',
      entityType: ['scheduled-task', 'scheduled-tasks'],
      component: 'fa:calendar-times-o'
    },
    {
      id: 'role-request-info',
      type: 'entity-info',
      entityType: ['roleRequest', 'IdmRoleRequest', 'IdmRoleRequestDto'],
      component: require('./src/components/advanced/RoleRequestInfo/RoleRequestInfo').default,
      manager: require('./src/redux').RoleRequestManager
    },
    {
      id: 'password-info',
      type: 'entity-info',
      entityType: ['password', 'IdmPassword', 'IdmPasswordDto'],
      component: require('./src/components/advanced/PasswordInfo/PasswordInfo').default,
      manager: require('./src/redux').PasswordManager
    },
    {
      id: 'password-icon',
      type: 'icon',
      entityType: ['password', 'password-policies', 'password-policy'],
      component: 'fa:lock'
    },
    {
      id: 'audit-icon',
      type: 'icon',
      entityType: ['audit'],
      component: 'fa:history'
    },
    {
      id: 'setting-icon',
      type: 'icon',
      entityType: ['setting'],
      component: 'fa:cog'
    },
    {
      id: 'configuration-info',
      type: 'entity-info',
      entityType: ['configuration', 'IdmConfiguration', 'IdmConfigurationDto'],
      component: require('./src/components/advanced/ConfigurationInfo/ConfigurationInfo').default,
      manager: require('./src/redux').ConfigurationManager
    },
    {
      id: 'automatic-role-tree-select-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'UUID',
      faceType: 'AUTOMATIC-ROLE-TREE-SELECT',
      component: require('./src/components/advanced/Form/SelectBoxFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.AUTOMATIC-ROLE-TREE-SELECT',
      manager: require('./src/redux').RoleTreeNodeManager
    },
    {
      id: 'automatic-role-attribute-select-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'UUID',
      faceType: 'AUTOMATIC-ROLE-ATTRIBUTE-SELECT',
      component: require('./src/components/advanced/Form/SelectBoxFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.AUTOMATIC-ROLE-ATTRIBUTE-SELECT',
      manager: require('./src/redux').AutomaticRoleAttributeManager
    },
    {
      id: 'operation-state-enum-select-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'ENUMERATION',
      faceType: 'OPERATION-STATE-ENUM',
      component: require('./src/components/advanced/Form/EnumSelectBoxFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.OPERATION-STATE-ENUM',
      enum: require('./src/enums/OperationStateEnum')
    },
    {
      id: 'workflow-definition-select-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'SHORTTEXT',
      faceType: 'WORKFLOW-DEFINITION-SELECT',
      component: require('./src/components/advanced/Form/SelectBoxFormAttributeRenderer'),
      labelKey: 'core:component.advanced.EavForm.faceType.WORKFLOW-DEFINITION-SELECT',
      manager: require('./src/redux').WorkflowProcessDefinitionManager
    }
  ]
};
