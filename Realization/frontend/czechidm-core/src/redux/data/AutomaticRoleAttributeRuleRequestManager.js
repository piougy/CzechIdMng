import EntityManager from './EntityManager';
import { AutomaticRoleAttributeRuleRequestService } from '../../services';

export default class AutomaticRoleAttributeRuleRequestManager extends EntityManager {

  constructor() {
    super();
    this.service = new AutomaticRoleAttributeRuleRequestService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'AutomaticRoleAttributeRuleRequest';
  }

  getCollectionType() {
    return 'automaticRoleAttributeRuleRequests';
  }
}
