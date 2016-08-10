import EntityManager from './EntityManager';
import { OrganizationService } from '../../services';

export default class OrganizationManager extends EntityManager {

  constructor() {
    super();
    this.service = new OrganizationService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Organization'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'organizations';
  }
}
