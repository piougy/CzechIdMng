
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
      id: 'remote-server-info',
      type: 'entity-info',
      entityType: ['remoteServer', 'SysRemoteServer', 'SysRemoteServerDto', 'SysConnectorServerDto', 'SysConnectorServer'],
      component: require('./src/components/RemoteServerInfo/RemoteServerInfo').default,
      manager: require('./src/redux').RemoteServerManager
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
      id: 'echo-item-info',
      type: 'entity-info',
      entityType: ['echo', 'echoItem'],
      component: require('./src/components/EchoItemInfo/EchoItemInfo').default,
      manager: null
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
    },
    {
      id: 'csv-connector-icon',
      type: 'icon',
      entityType: ['csv'],
      component: require('./src/components/CsvConnectorIcon/CsvConnectorIcon')
    },
    {
      id: 'ad-connector-icon',
      type: 'icon',
      entityType: ['ad', 'ad-connector-icon'],
      component: require('./src/components/AdConnectorIcon/AdConnectorIcon')
    },
    {
      id: 'ldap-connector-icon',
      type: 'icon',
      entityType: ['ldap', 'ldap-connector-icon'],
      component: require('./src/components/LdapConnectorIcon/LdapConnectorIcon')
    },
    {
      id: 'scripted-sql-connector-icon',
      type: 'icon',
      entityType: ['scripted-sql-connector-icon'],
      component: require('./src/components/ScriptedSqlConnectorIcon/ScriptedSqlConnectorIcon')
    },
    {
      id: 'mssql-connector-icon',
      type: 'icon',
      entityType: ['ad', 'mssql-connector'],
      component: require('./src/components/MsSqlConnectorIcon/MsSqlConnectorIcon')
    },
    {
      id: 'mysql-connector-icon',
      type: 'icon',
      entityType: ['mysql', 'mysql-connector'],
      component: require('./src/components/MySqlConnectorIcon/MySqlConnectorIcon')
    },
    {
      id: 'default-connector-icon',
      type: 'icon',
      entityType: ['default-connector'],
      component: require('./src/components/DefaultConnectorIcon/DefaultConnectorIcon')
    },
    {
      id: 'postgresql-connector-icon',
      type: 'icon',
      entityType: ['postgresql-connector'],
      component: require('./src/components/PostgreSqlConnectorIcon/PostgreSqlConnectorIcon')
    },
    {
      id: 'csv-connector-type',
      type: 'connector-type',
      entityType: ['csv-connector-type'],
      component: require('./src/content/wizard/connectorType/CsvSystemWizard')
    },
    {
      id: 'postgresql-connector-type',
      type: 'connector-type',
      entityType: ['postgresql-connector-type'],
      component: require('./src/content/wizard/connectorType/JdbcSqlSystemWizard')
    },
    {
      id: 'mssql-connector-type',
      type: 'connector-type',
      entityType: ['mssql-connector-type'],
      component: require('./src/content/wizard/connectorType/JdbcSqlSystemWizard')
    },
    {
      id: 'mysql-connector-type',
      type: 'connector-type',
      entityType: ['mysql-connector-type'],
      component: require('./src/content/wizard/connectorType/JdbcSqlSystemWizard')
    },
    {
      id: 'ad-connector-type',
      type: 'connector-type',
      entityType: ['ad-connector-type'],
      component: require('./src/content/wizard/connectorType/AdUserSystemWizard/AdUserSystemWizard')
    },
    {
      id: 'ad-winrm-connector-type',
      type: 'connector-type',
      entityType: ['ad-winrm-connector-type'],
      component: require('./src/content/wizard/connectorType/AdUserSystemWizard/AdUserSystemWizard')
    },
    {
      id: 'remote-server-icon',
      type: 'icon',
      entityType: ['server', 'servers'],
      component: 'fa:server'
    },
    {
      id: 'remote-server-select-box',
      type: 'entity-select-box',
      priority: 0,
      localizationKey: 'acc.entity.RemoteServer',
      entityType: [ 'remoteServer' ],
      searchInFields: ['host', 'description'],
      manager: require('./src/redux').RemoteServerManager
    }
  ]
};
