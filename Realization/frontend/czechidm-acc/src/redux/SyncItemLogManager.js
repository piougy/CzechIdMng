import { Managers } from 'czechidm-core';
import { SyncItemLogService } from '../services';

const service = new SyncItemLogService();

export default class SyncItemLogManager
 extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'SyncItemLog';
  }

  getCollectionType() {
    return 'syncItemLogs';
  }
}
