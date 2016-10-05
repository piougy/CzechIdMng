import SystemService from './SystemService';
import RoleSystemService from './RoleSystemService';
import SystemEntityService from './SystemEntityService';
import AccountService from './AccountService';
import IdentityAccountService from './IdentityAccountService';

const ServiceRoot = {
  SystemService,
  RoleSystemService,
  SystemEntityService,
  AccountService,
  IdentityAccountService
};

ServiceRoot.version = '0.1.0';
module.exports = ServiceRoot;
