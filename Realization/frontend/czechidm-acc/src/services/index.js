import SystemService from './SystemService';
import RoleSystemService from './RoleSystemService';
import SystemEntityService from './SystemEntityService';

const ServiceRoot = {
  SystemService,
  RoleSystemService,
  SystemEntityService
};

ServiceRoot.version = '0.1.0';
module.exports = ServiceRoot;
