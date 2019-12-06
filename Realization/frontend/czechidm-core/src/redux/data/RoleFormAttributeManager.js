import AbstractRequestManager from './AbstractRequestManager';
import { RoleFormAttributeService } from '../../services';

/**
 * Role form attribute
 *
 * @author Vít Švanda
 */
export default class RoleFormAttributeManager extends AbstractRequestManager {

  constructor() {
    super();
    this.service = new RoleFormAttributeService();
  }

  getService() {
    return this.service;
  }

  /**
  * Using in the request
  */
  getEntitySubType() {
    return 'RoleFormAttribute';
  }

  getCollectionType() {
    return 'roleFormAttributes';
  }
}
