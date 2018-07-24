import EntityManager from './EntityManager';
import { RequestItemService } from '../../services';

export default class RequestItemManager extends EntityManager {

  constructor() {
    super();
    this.service = new RequestItemService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'RequestItem';
  }

  getCollectionType() {
    return 'request-items';
  }
}
