import EntityManager from './EntityManager';
import { AutomaticRoleAttributeRuleService } from '../../services';

/**
 * Rules for automatic role manager by attribute
 *
 * @author Ondrej Kopr
 */
export default class AutomaticRoleAttributeRuleManager extends EntityManager {

  constructor() {
    super();
    this.service = new AutomaticRoleAttributeRuleService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'AutomaticRoleAttributeRule';
  }

  getCollectionType() {
    return 'automaticRoleAttributeRules';
  }
}
