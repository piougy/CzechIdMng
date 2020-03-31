import * as Utils from '../../utils';
import { IdentityProjectionService } from '../../services';
import SecurityManager from '../security/SecurityManager';
import IdentityManager from './IdentityManager';

/**
 * Manager for identity projection - post (~createEntity) / get (fetchEntity) is supported now only.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
export default class IdentityProjectionManager extends IdentityManager {

  constructor() {
    super();
    this.identityProjectionService = new IdentityProjectionService();
  }

  getService() {
    return this.identityProjectionService;
  }

  getEntityType() {
    return 'IdentityProjection';
  }

  /**
   * Authorization evaluator helper - evaluates save permission on given entity
   * If entity is null - IDENTITY_CREATE is evaluated
   * permissions are embedded in projection
   *
   * @param  {object} entity
   * @return {bool}
   */
  canSave(projection) {
    if (!this.getGroupPermission()) {
      return false;
    }
    if (!projection || Utils.Entity.isNew(projection.identity)) {
      return SecurityManager.hasAuthority(`${ this.getGroupPermission() }_CREATE`);
    }
    return Utils.Permission.hasPermission(projection._permissions, 'UPDATE');
  }

  /**
   * Load projection by id from server.
   *
   * @param  {string|number} id - Entity identifier
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @param  {func} cb - function will be called after entity is fetched
   * @return {object} - action
   */
  fetchProjection(id, uiKey = null, cb = null) {
    return (dispatch, getState) => {
      if (getState().security.userContext.isExpired) {
        return dispatch({
          type: IdentityManager.EMPTY
        });
      }
      //
      uiKey = this.resolveUiKey(uiKey, id);
      dispatch(this.requestEntity(id, uiKey));
      this.getService()
        .getById(id)
        .then(json => {
          dispatch(this.receiveEntity(id, json, uiKey, cb));
        })
        .catch(error => {
          dispatch(this.receiveError({ id }, uiKey, error, cb));
        });
    };
  }

  /**
   * Save projection.
   *
   * @param  {object} entity - Entity to patch
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @param  {func} cb - function will be called after entity is patched or error occured
   * @return {object} - action
   */
  saveProjection(entity, uiKey = null, cb = null) {
    if (!entity) {
      return {
        type: IdentityManager.EMPTY
      };
    }
    uiKey = this.resolveUiKey(uiKey, entity.id);
    return (dispatch) => {
      dispatch(this.requestEntity(entity.id, uiKey));
      this.getService()
        .create(entity)
        .then(json => {
          dispatch(this.receiveEntity(json.id, json, uiKey, cb));
        })
        .catch(error => {
          dispatch(this.receiveError(entity, uiKey, error, cb));
        });
    };
  }
}
