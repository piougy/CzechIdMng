import { Managers } from 'czechidm-core';
import { RoleSystemService } from '../services';

const service = new RoleSystemService();

export default class RoleSystemManager extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'RoleSystem'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'roleSystems';
  }
}
