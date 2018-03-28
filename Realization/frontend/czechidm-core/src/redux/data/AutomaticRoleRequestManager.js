import EntityManager from './EntityManager';
import { AutomaticRoleRequestService } from '../../services';

export default class AutomaticRoleRequestManager extends EntityManager {

  constructor() {
    super();
    this.service = new AutomaticRoleRequestService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'AutomaticRoleRequest';
  }

  getCollectionType() {
    return 'automaticRoleRequests';
  }
}
