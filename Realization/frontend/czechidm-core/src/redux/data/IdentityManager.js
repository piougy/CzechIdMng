import Immutable from 'immutable';
import EntityManager from './EntityManager';
import SecurityManager from '../security/SecurityManager';
import { IdentityService } from '../../services';
import DataManager from './DataManager';
import FormInstance from '../../domain/FormInstance';

/**
 * Manager for identity fetching
 */
export default class IdentityManager extends EntityManager {

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
   * Return true, if given identity is exterine
   */
  isExterne(identity) {
    return this.getService().isExterne(identity);
  }

  getFullName(identity) {
    return this.getService().getFullName(identity);
  }

  /**
   * Who can edit identity - just for ui, rest is secured as well
   *
   * @return {Immutable.Map<string, boolean>} UI elements, which is editable <key, boolean>
   */
  canEditMap(userContext) {
    let canEditMap = new Immutable.Map();
    canEditMap = canEditMap.set('isSaveEnabled', false);
    //
    // super admin or user's garant can edit user profile
    if (SecurityManager.isAdmin(userContext)) {
      canEditMap = canEditMap.set('isSaveEnabled', true);
    }
    return canEditMap;
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
        dispatch(this.flashMessagesManager.addMessage({
          level: successUsernames.length === usernames.length ? 'success' : 'info',
          message: this.i18n(`content.identities.action.${bulkActionName}.success`, { usernames: successUsernames.join(', ') })
        }));
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
   * Load form instance (definition + values) by given identity
   *
   * @param  {string} id identity identifier
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  fetchFormInstance(id, uiKey, cb = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));

      const formDefinitionPromise = this.getService().getFormDefinition(id);
      const formValuesPromise = this.getService().getFormValues(id);

      Promise.all([formDefinitionPromise, formValuesPromise])
        .then((jsons) => {
          const formDefinition = jsons[0];
          const formValues = jsons[1]._embedded.idmIdentityFormValues;

          const formInstance = new FormInstance(formDefinition, formValues);

          dispatch(this.dataManager.receiveData(uiKey, formInstance));
          if (cb) {
            cb(formInstance);
          }
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }

  /**
   * Saves form values
   *
   * @param  {string} id identity identifier
   * @param  {arrayOf(entity)} values filled form values
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  saveFormValues(id, values, uiKey, cb = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().saveFormValues(id, values)
      .then(() => {
        dispatch(this.fetchFormInstance(id, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(null, uiKey, error, cb));
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
