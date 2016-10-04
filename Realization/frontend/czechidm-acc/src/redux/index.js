import SystemManager from './SystemManager';
import RoleSystemManager from './RoleSystemManager';
import SystemEntityManager from './SystemEntityManager';
import AccountManager from './AccountManager';

const ManagerRoot = {
  SystemManager,
  RoleSystemManager,
  SystemEntityManager,
  AccountManager
};

ManagerRoot.version = '0.0.1';
module.exports = ManagerRoot;
