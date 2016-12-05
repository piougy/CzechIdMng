import { Managers } from 'czechidm-core';
import { RoleSystemAttributeService } from '../services';

const service = new RoleSystemAttributeService();

export default class RoleSystemAttributeManager extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'RoleSystemAttribute'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'roleSystemAttributes';
  }
}
