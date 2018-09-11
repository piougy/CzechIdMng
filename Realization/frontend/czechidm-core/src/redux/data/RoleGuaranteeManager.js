import AbstractRequestManager from './AbstractRequestManager';
import { RoleGuaranteeService } from '../../services';

/**
 * Role guarantees
 *
 * @author Radek Tomiška
 */
export default class RoleGuaranteeManager extends AbstractRequestManager {

  constructor() {
    super();
    this.service = new RoleGuaranteeService();
  }

  getService() {
    return this.service;
  }

  /**
  * Using in the request
  */
  getEntitySubType() {
    return 'RoleGuarantee';
  }

  getCollectionType() {
    return 'roleGuarantees';
  }
}
