import EntityManager from './EntityManager';
import { RoleRequestService } from '../../services';

export default class RoleRequestManager extends EntityManager {

  constructor() {
    super();
    this.service = new RoleRequestService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'RoleRequest';
  }

  getCollectionType() {
    return 'roleRequests';
  }
}
