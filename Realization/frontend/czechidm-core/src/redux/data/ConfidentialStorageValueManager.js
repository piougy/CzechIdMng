import EntityManager from './EntityManager';
import { ConfidentialStorageValueService } from '../../services';

export default class ConfidentialStorageValueManager extends EntityManager {

  constructor() {
    super();
    this.service = new ConfidentialStorageValueService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'ConfidentialStorageValue';
  }

  getCollectionType() {
    return 'confidentialStorageValues';
  }
}
