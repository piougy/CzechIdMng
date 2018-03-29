import FormableEntityManager from './FormableEntityManager';
import SecurityManager from '../security/SecurityManager';
import { IdentityService } from '../../services';
import DataManager from './DataManager';
import * as Utils from '../../utils';

/**
 * Manager for identities fetching
 *
 * @author Radek Tomiška
 */
export default class IdentityManager extends FormableEntityManager {

  constructor() {
    super();
    this.identityService = new IdentityService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.identityService;
  }

  getEntityType() {
    return 'Identity'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'identities';
  }

  /**
   * Return resource identifier on FE (see BE - IdentifiableByName)
   *
   * @return {string} secondary identifier (unique property)
   */
  getIdentifierAlias() {
    return 'username';
  }

  getFullName(identity) {
    return this.getService().getFullName(identity);
  }

  /**
   * Sets user activity
   *
   * @param {array[string]} usernames selected usernames
   * @param {string} bulkActionName activate|deactivate
   */
  setUsersActivity(entities, bulkActionName) {
    return (dispatch) => {
      dispatch(
        this.startBulkAction(
          {
            name: bulkActionName,
            title: this.i18n(`content.identities.action.${bulkActionName}.header`, { count: entities.length })
          },
          entities.length
        )
      );
      const successEntities = [];
      entities.reduce((sequence, identity) => {
        return sequence.then(() => {
          if (bulkActionName === 'activate') {
            return this.getService().activate(identity.id);
          }
          return this.getService().deactivate(identity.id);
        }).then(json => {
          dispatch(this.updateBulkAction());
          successEntities.push(identity);
          // new entity to redux trimmed store
          json._trimmed = true;
          dispatch(this.receiveEntity(identity.id, json));
        }).catch(error => {
          dispatch(this.flashMessagesManager.addErrorMessage({ title: this.i18n(`content.identities.action.${bulkActionName}.error`, { username: this.getNiceLabel(identity) }) }, error));
          throw error;
        });
      }, Promise.resolve())
      .catch(() => {
        // nothing - message is propagated before
        // catch is before then - we want execute nex then clausule
      })
      .then(() => {
        if (successEntities.length > 0) {
          dispatch(this.flashMessagesManager.addMessage({
            level: successEntities.length === entities.length ? 'success' : 'info',
            message: this.i18n(`content.identities.action.${bulkActionName}.success`, { usernames: this.getNiceLabels(successEntities).join(', ') })
          }));
        }
        dispatch(this.stopBulkAction());
      });
    };
  }

  /**
   * Load username authorities from BE
   *
   * @param  {string} username
   * @param  {string} uiKey
   * @return {array[object]}
   */
  fetchAuthorities(username, uiKey) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getAuthorities(username)
        .then(json => {
          dispatch(this.dataManager.receiveData(uiKey, json));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Get given identity's main position in organization
   *
   * @param  {string} username
   * @param  {string} uiKey
   * @return {array[object]}
   */
  fetchWorkPosition(username, uiKey) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getWorkPosition(username)
        .then(json => {
          dispatch(this.dataManager.receiveData(uiKey, json));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Return true when currently logged user can change password
   *
   * @param  {string} passwordChangeType see consts bottom
   * @param  {arrayOf(string)} permissions logged identity's base permissions on selected identity
   * @return {bool}
   */
  canChangePassword(passwordChangeType, permissions) {
    if (!passwordChangeType || passwordChangeType === IdentityManager.PASSWORD_DISABLED) {
      // password cannot be changed by environment configuration
      return false;
    }
    if (SecurityManager.isAdmin()) {
      // admin
      return true;
    }
    return Utils.Permission.hasPermission(permissions, 'PASSWORDCHANGE');
  }

  upload(formData, identityId) {
    return this.getService().upload(formData, identityId);
  }

  download(identityId, cb) {
    return this.getService().download(identityId, cb);
  }

  deleteImage(identityId) {
    this.getService().deleteImage(identityId);
  }

  /**
   * PreValidates password
   *
   * @param  {string} requestData
   * @return
   */
  preValidate(requestData) {
    return this.identityService.preValidate(requestData);
  }
}

IdentityManager.PASSWORD_DISABLED = 'DISABLED';
IdentityManager.PASSWORD_ALL_ONLY = 'ALL_ONLY';
IdentityManager.PASSWORD_CUSTOM = 'CUSTOM';
