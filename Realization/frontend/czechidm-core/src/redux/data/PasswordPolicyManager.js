import EntityManager from './EntityManager';
import { PasswordPolicyService } from '../../services';

export default class PasswordPolicyManager extends EntityManager {

  constructor() {
    super();
    this.service = new PasswordPolicyService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'PasswordPolicy';
  }

  getCollectionType() {
    return 'passwordPolicies';
  }
}
