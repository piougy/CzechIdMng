
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
