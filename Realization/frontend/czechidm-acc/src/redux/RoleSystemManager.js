import { Managers } from 'czechidm-core';
import { RoleSystemService } from '../services';

const service = new RoleSystemService();

export default class RoleSystemManager extends Managers.AbstractRequestManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntitySubType() {
    return 'RoleSystem'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'roleSystems';
  }
}
