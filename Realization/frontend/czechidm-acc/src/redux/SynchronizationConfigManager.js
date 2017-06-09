import { Managers } from 'czechidm-core';
import { SynchronizationConfigService } from '../services';

const service = new SynchronizationConfigService();

export default class SynchronizationConfigManager
 extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'SynchronizationConfig';
  }

  getCollectionType() {
    return 'synchronizationConfigs';
  }
}
