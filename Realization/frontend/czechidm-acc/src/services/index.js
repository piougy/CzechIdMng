import SystemService from './SystemService';
import RoleSystemService from './RoleSystemService';
import SystemEntityService from './SystemEntityService';
import AccountService from './AccountService';
import IdentityAccountService from './IdentityAccountService';
import SchemaObjectClassService from './SchemaObjectClassService';

const ServiceRoot = {
  SystemService,
  RoleSystemService,
  SystemEntityService,
  AccountService,
  IdentityAccountService,
  SchemaObjectClassService
};

ServiceRoot.version = '0.1.0';
module.exports = ServiceRoot;
