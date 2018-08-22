import EntityManager from './EntityManager';
import { RoleCompositionService } from '../../services';

/**
 * Role composition - define busines role
 *
 * @author Radek Tomiška
 */
export default class RoleCompositionManager extends EntityManager {

  constructor() {
    super();
    this.service = new RoleCompositionService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'RoleComposition';
  }

  getCollectionType() {
    return 'roleCompositions';
  }
}
