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

  /**
   * Recalucalte given automatic role by attribute
   */
  recalculate(id, callback = null) {
    this.getService().recalculate(id, callback);
  }
}
