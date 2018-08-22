import EntityManager from './EntityManager';
import { IdentityRoleService, IdentityService } from '../../services';

const identityService = new IdentityService();

/**
 * Identity roles - assigned roles
 *
 * @author Radek Tomiška
 *
 */
export default class IdentityRoleManager extends EntityManager {

  constructor() {
    super();
    //
    this.service = new IdentityRoleService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'IdentityRole';
  }

  getCollectionType() {
    return 'identityRoles';
  }

  /**
   * Extended nice label
   *
   * @param  {entity} entity
   * @param  {boolean} showIdentity identity will be rendered.
   * @return {string}
   */
  getNiceLabel(entity, showIdentity = true) {
    return this.getService().getNiceLabel(entity, showIdentity);
  }

  /**
   * Fetch given identity roles
   */
  fetchRoles(username, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey);
    return (dispatch) => {
      dispatch(this.requestEntities(null, uiKey));
      identityService.getRoles(username)
      .then(json => {
        dispatch(this.receiveEntities(null, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError({}, uiKey, error, cb));
      });
    };
  }
}
