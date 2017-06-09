import { Managers } from 'czechidm-core';
import { SyncActionLogService } from '../services';

const service = new SyncActionLogService();

export default class SyncActionLogManager
 extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'SyncActionLog';
  }

  getCollectionType() {
    return 'syncActionLogs';
  }
}
