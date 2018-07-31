import EntityManager from './EntityManager';
import { UniversalRequestService } from '../../services';

export default class UniversalRequestManager extends EntityManager {

  constructor(requestId, originalManager) {
    super();
    this.requestId = requestId;
    this.originalManager = originalManager;
    this.service = new UniversalRequestService(this.requestId, this.originalManager.getService());
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return `Request-${this.originalManager.getEntityType()}`;
  }

  getCollectionType() {
    return this.originalManager.getCollectionType();
  }
}
