import EntityManager from './EntityManager';
import { RoleGuaranteeRoleService } from '../../services';

/**
 * Role guarantees - by role
 *
 * @author Radek Tomiška
 */
export default class RoleGuaranteeRoleManager extends EntityManager {

  constructor() {
    super();
    this.service = new RoleGuaranteeRoleService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'RoleGuaranteeRole';
  }

  getCollectionType() {
    return 'roleGuaranteeRoles';
  }
}
