import EntityManager from './EntityManager';
import { RoleCatalogueService } from '../../services';

export default class RoleCatalogueManager extends EntityManager {

  constructor() {
    super();
    this.service = new RoleCatalogueService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'RoleCatalogue';
  }

  getCollectionType() {
    return 'roleCatalogues';
  }

  /**
   * Return search parameters for roots endpoint.
   * Search all roots for roleCatalogue
   */
  getRootSearchParameters() {
    return this.getService().getRootSearchParameters();
  }

  /**
   * Return search parameters for children by parent id
   */
  getTreeSearchParameters() {
    return this.getService().getTreeSearchParameters();
  }
}
