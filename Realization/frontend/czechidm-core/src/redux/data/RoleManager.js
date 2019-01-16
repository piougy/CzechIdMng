import AbstractRequestFormableManager from './AbstractRequestFormableManager';
import { RoleService} from '../../services';
import DataManager from './DataManager';
import IncompatibleRoleManager from './IncompatibleRoleManager';

/**
 * Operations with RoleService
 *
 * @author Radek Tomiška
 */
export default class RoleManager extends AbstractRequestFormableManager {

  constructor() {
    super();
    this.service = new RoleService();
    this.dataManager = new DataManager();
    this.incompatibleRoleManager = new IncompatibleRoleManager();
  }

  getService() {
    return this.service;
  }

  /**
  * Using in the request
  */
  getEntitySubType() {
    return 'Role';
  }

  getCollectionType() {
    return 'roles';
  }

  getIdentifierAlias() {
    return 'code';
  }

  /**
   * Load available authorities from BE if needed. Available authorities can be changed, when some module is enabled / disabled.
   *
   * @param  {string} uiKey
   * @return {array[object]}
   */
  fetchAvailableAuthorities() {
    const uiKey = RoleManager.UI_KEY_AVAILABLE_AUTHORITIES;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getAvailableAuthorities()
        .then(json => {
          dispatch(this.dataManager.receiveData(uiKey, json));
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Load all (installed) authorities from BE if needed (all authorites can be changed just with BE restart).
   *
   * @param  {string} uiKey
   * @return {array[object]}
   */
  fetchAllAuthorities() {
    const uiKey = RoleManager.UI_KEY_ALL_AUTHORITIES;
    //
    return (dispatch, getState) => {
      const allAuthorities = DataManager.getData(getState(), uiKey);
      if (allAuthorities) {
        // we dont need to load them again
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getAllAuthorities()
          .then(json => {
            dispatch(this.dataManager.receiveData(uiKey, json));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.receiveError(null, uiKey, error));
          });
      }
    };
  }

  /**
   * Load form definition for role attributes
   *
   * @param  {string} id role identifier
   * @param {string} uiKey
   * @returns {action}
   */
  fetchAttributeFormDefinition(id, uiKey) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      //
      this.getService().getAttributeFormDefinition(id)
      .then(json => {
        dispatch(this.dataManager.receiveData(uiKey, json));
      })
      .catch(error => {
        // TODO: data uiKey
        dispatch(this.receiveError(null, uiKey, error));
      });
    };
  }

  /**
   * Incompatible roles are resolved from sub roles.
   *
   * @param  {string} id
   * @param  {string} uiKey
   * @return {array[object]}
   */
  fetchIncompatibleRoles(id, uiKey) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getIncompatibleRoles(id)
        .then(json => {
          dispatch(this.dataManager.receiveData(uiKey, json._embedded[this.incompatibleRoleManager.getCollectionType()]));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }
}

RoleManager.UI_KEY_AVAILABLE_AUTHORITIES = 'available-authorities';
RoleManager.UI_KEY_ALL_AUTHORITIES = 'all-authorities';
