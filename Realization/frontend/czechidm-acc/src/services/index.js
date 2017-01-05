import SystemService from './SystemService';
import RoleSystemService from './RoleSystemService';
import RoleSystemAttributeService from './RoleSystemAttributeService';
import SystemEntityService from './SystemEntityService';
import AccountService from './AccountService';
import IdentityAccountService from './IdentityAccountService';
import SchemaObjectClassService from './SchemaObjectClassService';
import SchemaAttributeService from './SchemaAttributeService';
import SchemaAttributeHandlingService from './SchemaAttributeHandlingService';
import SystemEntityHandlingService from './SystemEntityHandlingService';
import SynchronizationConfigService from './SynchronizationConfigService';
import SynchronizationLogService from './SynchronizationLogService';

const ServiceRoot = {
  SystemService,
  RoleSystemService,
  SystemEntityService,
  AccountService,
  IdentityAccountService,
  SchemaObjectClassService,
  SchemaAttributeService,
  SchemaAttributeHandlingService,
  SystemEntityHandlingService,
  RoleSystemAttributeService,
  SynchronizationConfigService,
  SynchronizationLogService
};

ServiceRoot.version = '0.1.0';
module.exports = ServiceRoot;
