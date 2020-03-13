
module.exports = {
  id: 'acc',
  name: 'Account managment',
  description: 'Components for account managment module',
  components: [
    {
      id: 'password-change-content',
      description: 'Adds change password on selected accounts',
      priority: 10,
      component: require('./src/content/identity/PasswordChangeAccounts')
    },
    {
      id: 'system-info',
      type: 'entity-info',
      entityType: ['system', 'SysSystem', 'SysSystemDto'],
      component: require('./src/components/SystemInfo/SystemInfo').default,
      manager: require('./src/redux').SystemManager
    },
    {
      id: 'schema-attribute-info',
      type: 'entity-info',
      entityType: ['schemaAttribute', 'SysSchemaAttribute', 'SysSchemaAttributeDto'],
      component: require('./src/components/SchemaAttributeInfo/SchemaAttributeInfo').default,
      manager: require('./src/redux').SchemaAttributeManager
    },
    {
      id: 'schema-info',
      type: 'entity-info',
      entityType: ['schema', 'SysSchemaObjectClass', 'SysSchemaObjectClassDto'],
      component: require('./src/components/SchemaInfo/SchemaInfo').default,
      manager: require('./src/redux').SchemaObjectClassManager
    },
    {
      id: 'attribute-mapping-info',
      type: 'entity-info',
      entityType: ['systemAttributeMapping', 'SysSystemAttributeMapping', 'SysSystemAttributeMappingDto'],
      component: require('./src/components/SystemAttributeMappingInfo/SystemAttributeMappingInfo').default,
      manager: require('./src/redux').SystemAttributeMappingManager
    },
    {
      id: 'mapping-info',
      type: 'entity-info',
      entityType: ['systemMapping', 'SysSystemMapping', 'SysSystemMappingDto'],
      component: require('./src/components/SystemMappingInfo/SystemMappingInfo').default,
      manager: require('./src/redux').SystemMappingManager
    },
    {
      id: 'sync-config-info',
      type: 'entity-info',
      entityType: ['SysSyncIdentityConfig', 'SysSyncIdentityConfigDto',
        'SysSyncConfig', 'SysSyncConfigDto', 'SysSyncContractConfig', 'SysSyncContractConfigDto'],
      component: require('./src/components/SyncConfigInfo/SyncConfigInfo').default,
      manager: require('./src/redux').SynchronizationConfigManager
    },
    {
      id: 'break-config-info',
      type: 'entity-info',
      entityType: ['provisioningBreakConfig', 'SysProvisioningBreakConfig', 'SysProvisioningBreakConfigDto'],
      component: require('./src/components/BreakConfigInfo/BreakConfigInfo').default,
      manager: require('./src/redux').ProvisioningBreakConfigManager
    },
    {
      id: 'break-config-recipient-info',
      type: 'entity-info',
      entityType: ['provisioningBreakRecipient', 'SysProvisioningBreakRecipient', 'SysProvisioningBreakRecipientDto'],
      component: require('./src/components/BreakConfigRecipientInfo/BreakConfigRecipientInfo').default,
      manager: require('./src/redux').ProvisioningBreakRecipientManager
    },
    {
      id: 'system-select-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'UUID',
      faceType: 'SYSTEM-SELECT',
      component: require('czechidm-core/src/components/advanced/Form/SelectBoxFormAttributeRenderer'),
      labelKey: 'acc:component.advanced.EavForm.faceType.SYSTEM-SELECT',
      manager: require('./src/redux').SystemManager
    },
    {
      id: 'target-system-icon',
      type: 'icon',
      entityType: ['system', 'systems'],
      component: 'link'
    },
    {
      id: 'synchronization-config-select-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'UUID',
      faceType: 'SYNCHRONIZATION-CONFIG-SELECT',
      component: require('czechidm-core/src/components/advanced/Form/SelectBoxFormAttributeRenderer'),
      labelKey: 'acc:component.advanced.EavForm.faceType.SYNCHRONIZATION-CONFIG-SELECT',
      manager: require('./src/redux').SynchronizationConfigManager
    },
    {
      id: 'synchronization-icon',
      type: 'icon',
      entityType: ['synchronization', 'synchronizations'],
      component: 'fa:exchange'
    }
  ]
};
