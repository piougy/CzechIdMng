import EntityManager from './EntityManager';
import { RoleCatalogueRoleService } from '../../services';

/**
 * Role catalogues - asigend role
 *
 * @author Radek Tomiška
 */
export default class RoleCatalogueRoleManager extends EntityManager {

  constructor() {
    super();
    this.service = new RoleCatalogueRoleService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'RoleCatalogueRole';
  }

  getCollectionType() {
    return 'roleCatalogueRoles';
  }
}
