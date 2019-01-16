import EntityManager from './EntityManager';
import { RoleRequestService } from '../../services';
import IncompatibleRoleManager from './IncompatibleRoleManager';

export default class RoleRequestManager extends EntityManager {

  constructor() {
    super();
    this.service = new RoleRequestService();
    this.incompatibleRoleManager = new IncompatibleRoleManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'RoleRequest';
  }

  getCollectionType() {
    return 'roleRequests';
  }

  copyRolesByIdentity(roleRequestByIdentity, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey, roleRequestByIdentity.roleRequest);
    return (dispatch) => {
      this.getService().copyRolesByIdentity(roleRequestByIdentity)
      .then(json => {
        // Return is newly update role request
        dispatch(this.receiveEntity(json.id, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(roleRequestByIdentity, uiKey, error, cb));
      });
    };
  }

  /**
   * Incompatible roles are resolved from currently assigned identity roles (which can logged used read) and the current request concepts.
   *
   * @param  {string} username
   * @param  {string} uiKey
   * @return {array[object]}
   */
  fetchIncompatibleRoles(requestId, uiKey) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getIncompatibleRoles(requestId)
        .then(json => {
          dispatch(this.dataManager.receiveData(uiKey, json._embedded[this.incompatibleRoleManager.getCollectionType()]));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }
}
