import { Managers } from 'czechidm-core';
import { SystemEntityHandlingService } from '../services';

const service = new SystemEntityHandlingService();

export default class SystemEntityHandlingManager
 extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'SystemEntityHandling';
  }

  getCollectionType() {
    return 'systemEntitiesHandling';
  }
}
