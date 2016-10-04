import SystemService from './SystemService';
import RoleSystemService from './RoleSystemService';
import SystemEntityService from './SystemEntityService';
import AccountService from './AccountService';

const ServiceRoot = {
  SystemService,
  RoleSystemService,
  SystemEntityService,
  AccountService
};

ServiceRoot.version = '0.1.0';
module.exports = ServiceRoot;
