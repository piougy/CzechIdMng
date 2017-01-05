import { Managers } from 'czechidm-core';
import { SynchronizationLogService } from '../services';

const service = new SynchronizationLogService();

export default class SynchronizationLogManager
 extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'SynchronizationLog';
  }

  getCollectionType() {
    return 'synchronizationLogs';
  }
}
