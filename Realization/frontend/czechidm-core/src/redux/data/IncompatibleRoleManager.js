import AbstractRequestManager from './AbstractRequestManager';
import { IncompatibleRoleService } from '../../services';

/**
 * Incompatible role - defines Segregation of Duties.
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class IncompatibleRoleManager extends AbstractRequestManager {

  constructor() {
    super();
    this.service = new IncompatibleRoleService();
  }

  getService() {
    return this.service;
  }

  /**
  * Using in the request
  */
  getEntitySubType() {
    return 'IncompatibleRole';
  }

  getCollectionType() {
    return 'incompatibleRoles';
  }
}
