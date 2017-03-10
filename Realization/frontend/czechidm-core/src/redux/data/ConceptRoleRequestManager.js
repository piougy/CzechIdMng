import EntityManager from './EntityManager';
import { ConceptRoleRequestService } from '../../services';

export default class ConceptRoleRequestManager extends EntityManager {

  constructor() {
    super();
    this.service = new ConceptRoleRequestService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'ConceptRoleRequest';
  }

  getCollectionType() {
    return 'conceptRoleRequests';
  }
}
