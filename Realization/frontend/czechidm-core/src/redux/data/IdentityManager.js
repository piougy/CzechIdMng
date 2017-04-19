import FormableEntityManager from './FormableEntityManager';
import SecurityManager from '../security/SecurityManager';
import { IdentityService } from '../../services';
import DataManager from './DataManager';

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
  setUsersActivity(usernames, bulkActionName) {
    return (dispatch, getState) => {
      dispatch(
        this.startBulkAction(
          {
            name: bulkActionName,
            title: this.i18n(`content.identities.action.${bulkActionName}.header`, { count: usernames.length })
          },
          usernames.length
        )
      );
      const successUsernames = [];
      usernames.reduce((sequence, username) => {
        return sequence.then(() => {
          if (bulkActionName === 'activate') {
            return this.getService().activate(username);
          }
          return this.getService().deactivate(username);
        }).then(json => {
          dispatch(this.updateBulkAction());
          successUsernames.push(this.getEntity(getState(), username).username);
          // new entity to redux trimmed store
          json._trimmed = true;
          dispatch(this.receiveEntity(username, json));
        }).catch(error => {
          dispatch(this.flashMessagesManager.addErrorMessage({ title: this.i18n(`content.identities.action.${bulkActionName}.error`, { username }) }, error));
          throw error;
        });
      }, Promise.resolve())
      .catch(() => {
        // nothing - message is propagated before
        // catch is before then - we want execute nex then clausule
      })
      .then(() => {
        if (successUsernames.lengt > 0) {
          dispatch(this.flashMessagesManager.addMessage({
            level: successUsernames.length === usernames.length ? 'success' : 'info',
            message: this.i18n(`content.identities.action.${bulkActionName}.success`, { usernames: successUsernames.join(', ') })
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

  static canChangePassword(userContext, entityId, passwordChangeType) {
    return (passwordChangeType && passwordChangeType !== IdentityManager.PASSWORD_DISABLED && entityId === userContext.username) || SecurityManager.isAdmin(userContext);
  }
}

IdentityManager.PASSWORD_DISABLED = 'DISABLED';
IdentityManager.PASSWORD_ALL_ONLY = 'ALL_ONLY';
IdentityManager.PASSWORD_CUSTOM = 'CUSTOM';
