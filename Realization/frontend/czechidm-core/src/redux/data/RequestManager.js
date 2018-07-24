import EntityManager from './EntityManager';
import { RequestService } from '../../services';

export default class RequestManager extends EntityManager {

  constructor() {
    super();
    this.service = new RequestService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Request';
  }

  getCollectionType() {
    return 'requests';
  }
}
