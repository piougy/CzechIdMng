import EntityManager from './EntityManager';
import { RoleGuaranteeService } from '../../services';

/**
 * Role guarantees
 *
 * @author Radek Tomiška
 */
export default class RoleGuaranteeManager extends EntityManager {

  constructor() {
    super();
    this.service = new RoleGuaranteeService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'RoleGuarantee';
  }

  getCollectionType() {
    return 'roleGuarantees';
  }
}
