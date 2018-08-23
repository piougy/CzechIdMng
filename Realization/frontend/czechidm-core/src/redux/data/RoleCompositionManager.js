import AbstractRequestManager from './AbstractRequestManager';
import { RoleCompositionService } from '../../services';

/**
 * Role composition - define busines role
 *
 * @author Radek Tomiška
 */
export default class RoleCompositionManager extends AbstractRequestManager {

  constructor() {
    super();
    this.service = new RoleCompositionService();
  }

  getService() {
    return this.service;
  }

  /**
  * Using in the request
  */
  getEntitySubType() {
    return 'RoleComposition';
  }

  getCollectionType() {
    return 'roleCompositions';
  }
}
