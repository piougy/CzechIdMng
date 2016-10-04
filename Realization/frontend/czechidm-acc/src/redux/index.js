import SystemManager from './SystemManager';
import RoleSystemManager from './RoleSystemManager';
import SystemEntityManager from './SystemEntityManager';

const ManagerRoot = {
  SystemManager,
  RoleSystemManager,
  SystemEntityManager
};

ManagerRoot.version = '0.0.1';
module.exports = ManagerRoot;
