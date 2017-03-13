import FormableEntityManager from './FormableEntityManager';
import { IdentityContractService, IdentityService } from '../../services';

const service = new IdentityContractService();
const identityService = new IdentityService();

/**
 * Identity contracts
 *
 * @author Radek Tomiška
 */
export default class IdentityContractManager extends FormableEntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'IdentityContract'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'identityContracts';
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

  // TODO: use force filters and search instread? Security on identityContracts endpoint?
  fetchContracts(username, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey);
    return (dispatch) => {
      dispatch(this.requestEntities(null, uiKey));
      identityService.getContracts(username)
      .then(json => {
        dispatch(this.receiveEntities(null, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError({}, uiKey, error, cb));
      });
    };
  }
}
