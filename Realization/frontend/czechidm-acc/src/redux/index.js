import SystemManager from './SystemManager';
import RoleSystemManager from './RoleSystemManager';
import RoleSystemAttributeManager from './RoleSystemAttributeManager';
import SystemEntityManager from './SystemEntityManager';
import AccountManager from './AccountManager';
import IdentityAccountManager from './IdentityAccountManager';
import SchemaObjectClassManager from './SchemaObjectClassManager';
import SchemaAttributeManager from './SchemaAttributeManager';
import SchemaAttributeHandlingManager from './SchemaAttributeHandlingManager';
import SystemEntityHandlingManager from './SystemEntityHandlingManager';
import SynchronizationLogManager from './SynchronizationLogManager';
import SynchronizationConfigManager from './SynchronizationConfigManager';
import ProvisioningOperationManager from './ProvisioningOperationManager';
import SyncActionLogManager from './SyncActionLogManager';
import SyncItemLogManager from './SyncItemLogManager';

const ManagerRoot = {
  SystemManager,
  RoleSystemManager,
  SystemEntityManager,
  AccountManager,
  IdentityAccountManager,
  SchemaObjectClassManager,
  SchemaAttributeManager,
  SchemaAttributeHandlingManager,
  SystemEntityHandlingManager,
  RoleSystemAttributeManager,
  SynchronizationLogManager,
  SynchronizationConfigManager,
  ProvisioningOperationManager,
  SyncActionLogManager,
  SyncItemLogManager
};

ManagerRoot.version = '0.0.1';
module.exports = ManagerRoot;
