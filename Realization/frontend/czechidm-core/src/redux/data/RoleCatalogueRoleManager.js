import AbstractRequestManager from './AbstractRequestManager';
import { RoleCatalogueRoleService } from '../../services';

/**
 * Role catalogues - asigend role
 *
 * @author Radek Tomiška
 */
export default class RoleCatalogueRoleManager extends AbstractRequestManager {

  constructor() {
    super();
    this.service = new RoleCatalogueRoleService();
  }

  getService() {
    return this.service;
  }

  /**
  * Using in the request
  */
  getEntitySubType() {
    return 'RoleCatalogueRole';
  }

  getCollectionType() {
    return 'roleCatalogueRoles';
  }
}
