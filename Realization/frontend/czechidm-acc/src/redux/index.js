import SystemManager from './SystemManager';
import RoleSystemManager from './RoleSystemManager';
import SystemEntityManager from './SystemEntityManager';
import AccountManager from './AccountManager';
import IdentityAccountManager from './IdentityAccountManager';
import SchemaObjectClassManager from './SchemaObjectClassManager';
import SchemaAttributeManager from './SchemaAttributeManager';
import SchemaAttributeHandlingManager from './SchemaAttributeHandlingManager';
import SystemEntityHandlingManager from './SystemEntityHandlingManager';

const ManagerRoot = {
  SystemManager,
  RoleSystemManager,
  SystemEntityManager,
  AccountManager,
  IdentityAccountManager,
  SchemaObjectClassManager,
  SchemaAttributeManager,
  SchemaAttributeHandlingManager,
  SystemEntityHandlingManager
};

ManagerRoot.version = '0.0.1';
module.exports = ManagerRoot;
