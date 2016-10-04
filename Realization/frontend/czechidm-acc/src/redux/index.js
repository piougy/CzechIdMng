import SystemManager from './SystemManager';
import RoleSystemManager from './RoleSystemManager';
import SystemEntityManager from './SystemEntityManager';
import AccountManager from './AccountManager';
import IdentityAccountManager from './IdentityAccountManager';

const ManagerRoot = {
  SystemManager,
  RoleSystemManager,
  SystemEntityManager,
  AccountManager,
  IdentityAccountManager
};

ManagerRoot.version = '0.0.1';
module.exports = ManagerRoot;
