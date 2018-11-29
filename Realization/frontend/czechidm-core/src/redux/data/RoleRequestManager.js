import EntityManager from './EntityManager';
import { RoleRequestService } from '../../services';

export default class RoleRequestManager extends EntityManager {

  constructor() {
    super();
    this.service = new RoleRequestService();
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
        dispatch(this.receiveEntity(roleRequestByIdentity.roleRequest, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(roleRequestByIdentity, uiKey, error, cb));
      });
    };
  }
}
