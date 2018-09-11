import { Managers } from 'czechidm-core';
import { RoleSystemAttributeService } from '../services';

const service = new RoleSystemAttributeService();

export default class RoleSystemAttributeManager extends Managers.AbstractRequestManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntitySubType() {
    return 'RoleSystemAttribute'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'roleSystemAttributes';
  }
}
