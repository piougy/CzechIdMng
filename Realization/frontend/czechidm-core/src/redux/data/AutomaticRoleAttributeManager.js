import EntityManager from './EntityManager';
import { AutomaticRoleAttributeService } from '../../services';

/**
 * Automatic role manager by attribute
 *
 * @author Ondrej Kopr
 */
export default class AutomaticRoleAttributeManager extends EntityManager {

  constructor() {
    super();
    this.service = new AutomaticRoleAttributeService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'AutomaticRoleAttribute';
  }

  getCollectionType() {
    return 'automaticRoleAttributes';
  }

  recalculate(id) {
    this.getService().recalculate(id);
  }
}
