import AbstractRequestManager from './AbstractRequestManager';
import { RoleGuaranteeRoleService } from '../../services';

/**
 * Role guarantees - by role
 *
 * @author Radek Tomiška
 */
export default class RoleGuaranteeRoleManager extends AbstractRequestManager {

  constructor() {
    super();
    this.service = new RoleGuaranteeRoleService();
  }

  getService() {
    return this.service;
  }

  /**
  * Using in the request
  */
  getEntitySubType() {
    return 'RoleGuaranteeRole';
  }

  getCollectionType() {
    return 'roleGuaranteeRoles';
  }
}
