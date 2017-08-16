import SystemService from './SystemService';
import RoleSystemService from './RoleSystemService';
import RoleSystemAttributeService from './RoleSystemAttributeService';
import SystemEntityService from './SystemEntityService';
import AccountService from './AccountService';
import IdentityAccountService from './IdentityAccountService';
import SchemaObjectClassService from './SchemaObjectClassService';
import SchemaAttributeService from './SchemaAttributeService';
import SystemAttributeMappingService from './SystemAttributeMappingService';
import SystemMappingService from './SystemMappingService';
import SynchronizationConfigService from './SynchronizationConfigService';
import SynchronizationLogService from './SynchronizationLogService';
import ProvisioningOperationService from './ProvisioningOperationService';
import ProvisioningArchiveService from './ProvisioningArchiveService';
import SyncActionLogService from './SyncActionLogService';
import SyncItemLogService from './SyncItemLogService';
import RoleAccountService from './RoleAccountService';
import TreeAccountService from './TreeAccountService';

const ServiceRoot = {
  SystemService,
  RoleSystemService,
  SystemEntityService,
  AccountService,
  IdentityAccountService,
  SchemaObjectClassService,
  SchemaAttributeService,
  SystemAttributeMappingService,
  SystemMappingService,
  RoleSystemAttributeService,
  SynchronizationConfigService,
  SynchronizationLogService,
  ProvisioningOperationService,
  ProvisioningArchiveService,
  SyncActionLogService,
  SyncItemLogService,
  RoleAccountService,
  TreeAccountService
};

ServiceRoot.version = '0.1.0';
module.exports = ServiceRoot;
