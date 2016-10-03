import { Managers } from 'czechidm-core';
import { SystemEntityService } from '../services';

const service = new SystemEntityService();

export default class SystemEntityManager extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'SystemEntity'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'systemEntities';
  }
}
