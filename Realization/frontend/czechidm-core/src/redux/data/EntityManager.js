import { LocalizationService } from '../../services';
import FlashMessagesManager from '../flash/FlashMessagesManager';
import DataManager from './DataManager';
import SecurityManager from '../security/SecurityManager';
import * as Utils from '../../utils';
import SearchParameters from '../../domain/SearchParameters';

/**
 * action types
 * TODO: move to action constant (prevent manager import only because action type usage)
 */
export const REQUEST_ENTITIES = 'REQUEST_ENTITIES';
export const RECEIVE_ENTITIES = 'RECEIVE_ENTITIES';
export const REQUEST_ENTITY = 'REQUEST_ENTITY';
export const RECEIVE_ENTITY = 'RECEIVE_ENTITY';
export const DELETED_ENTITY = 'DELETED_ENTITY';
export const RECEIVE_ERROR = 'RECEIVE_ERROR';
export const CLEAR_ENTITIES = 'CLEAR_ENTITIES';
export const START_BULK_ACTION = 'START_BULK_ACTION';
export const PROCESS_BULK_ACTION = 'PROCESS_BULK_ACTION';
export const STOP_BULK_ACTION = 'STOP_BULK_ACTION';
export const RECEIVE_PERMISSIONS = 'RECEIVE_PERMISSIONS';
export const EMPTY = 'VOID_ACTION'; // dispatch cannot return null

/**
 * Encapsulate redux action for entity type
 *
 * @author Radek Tomiška
 */
export default class EntityManager {

  constructor() {
    if (this.getService === undefined) {
      throw new TypeError('Must override method getService()');
    }
    this.flashMessagesManager = new FlashMessagesManager();
    this.dataManager = new DataManager();
  }

  /**
   * Could be used for localiyation resolving etc.
   *
   * @return {string} moduleId
   */
  getModule() {
    // TODO: maybe could be resolved automatically from module descriptors and context?
    return 'core';
  }

  /**
   * Resource name
   *
   * @return {string}
   */
  getEntityType() {
    return 'Entity';
  }

  /**
   * Resources name (collection)
   *
   * @return {string}
   */
  getCollectionType() {
    return 'entites';
  }

  /**
   * Returns true, if `patch`  method is supported
   *
   * Added for enddpoints with dto - dto's doesn't support `patch` method for now
   *
   * @return {bool} Returns true, if `patch`  method is supported
   */
  supportsPatch() {
    return this.getService().supportsPatch();
  }

  /**
   * Added for enddpoints with authorization policies evaluation
   *
   * @return {bool} Returns true, when endpoint suppors uthorization policies evaluation
   */
  supportsAuthorization() {
    return this.getService().supportsAuthorization();
  }

  /**
   * Returns group permission for given manager / agenda
   *
   * @return {string} GroupPermission name
   */
  getGroupPermission() {
    return this.getService().getGroupPermission();
  }

  /**
   * Return resource identifier on FE (see BE - IdentifiableByName)
   *
   * @return {string} secondary identifier (unique property)
   */
  getIdentifierAlias() {
    return null;
  }

  /**
   * Textual entity reprezentation (~entity.toString())
   *
   * @param  {entity} entity
   * @return {string]}
   */
  getNiceLabel(entity) {
    if (!entity) {
      return null;
    }
    return this.getService().getNiceLabel(entity);
  }

  /**
   * Textual entities reprezentation (~entity.toString())
   *
   * @param  {array[entity]} entities
   * @return {array[string]}
   */
  getNiceLabels(entities) {
    return entities.map(entity => {
      return this.getNiceLabel(entity);
    });
  }

  /**
   * Somethimes is useful to construct self link for entity identification without whole entity. We know entity id + endpoint knows entityManager.
   *
   * @param  {number|string} entityId
   * @return {string} uri
   */
  getSelfLink(entityId) {
    if (!entityId) {
      return null;
    }
    return this.getService().getAbsoluteApiPath() + `/${entityId}`;
  }

  /**
   * Returns localized message
   * - for supported options see http://i18next.com/pages/doc_features.html
   *
   * @param  {string} key     localization key
   * @param  {object} options parameters
   * @return {string}         localized message
   */
  i18n(key, options) {
    return LocalizationService.i18n(key, options);
  }

  /**
   * Returns default uiKey by entity type, if given uiKey is not defined
   * This ui key can be used for check state of entities fetching etc.
   *
   * @param  {string} uiKey - ui key for loading indicator etc
   * @param  {string|number} id - Entity identifier
   * @return {string} - Returns uiKey
   */
  resolveUiKey(uiKey = null, id = null) {
    if (uiKey) {
      return uiKey;
    }
    if (id !== undefined && id !== null) {
      return `${this.getEntityType()}-${id}`;
    }
    return this.getEntityType();
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return this.getService().getDefaultSearchParameters();
  }

  /**
   * Returns given searchParameters or default, if searchParameters is not defined.
   * Fills range and sort, if is not defined in  given searchParameters
   *
   * @param  {object} searchParameters
   * @return {object} searchParameters
   */
  getSearchParameters(searchParameters) {
    return searchParameters || this.getDefaultSearchParameters();
  }

  /**
   * Merge search parameters - second sp has higher priority
   *
   * @param  {object} previousSearchParameters
   * @param  {object} newSearchParameters
   * @return {object} resultSearchParameters
   */
  mergeSearchParameters(previousSearchParameters, newSearchParameters) {
    return this.getService().mergeSearchParameters(previousSearchParameters, newSearchParameters);
  }

  /**
   * Load data from server
   */
  fetchEntities(searchParameters = null, uiKey = null, cb = null) {
    return (dispatch, getState) => {
      if (getState().security.userContext.isExpired) {
        return dispatch({
          type: EMPTY
        });
      }
      searchParameters = this.getSearchParameters(searchParameters);
      uiKey = this.resolveUiKey(uiKey);
      dispatch(this.requestEntities(searchParameters, uiKey));
      this.getService().search(searchParameters)
      .then(json => {
        dispatch(this.receiveEntities(searchParameters, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError({}, uiKey, error, cb));
      });
    };
  }

  /*
  * Request data
  */
  requestEntities(searchParameters, uiKey = null) {
    uiKey = this.resolveUiKey(uiKey);
    return {
      type: REQUEST_ENTITIES,
      searchParameters,
      uiKey
    };
  }

  /*
  * Receive data
  */
  receiveEntities(searchParameters, json, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey);
    const data = json._embedded[this.getCollectionType()] || [];
    return (dispatch) => {
      dispatch({
        type: RECEIVE_ENTITIES,
        entityType: this.getEntityType(),
        entities: data,
        total: json.page ? json.page.totalElements : data.length,
        searchParameters,
        uiKey
      });
      if (cb) {
        cb(json, null, uiKey);
      }
    };
  }

  /**
   * Load entity by id from server
   *
   * @param  {string|number} id - Entity identifier
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @param  {func} cb - function will be called after entity is fetched
   * @return {object} - action
   */
  fetchEntity(id, uiKey = null, cb = null) {
    return (dispatch, getState) => {
      if (getState().security.userContext.isExpired) {
        return dispatch({
          type: EMPTY
        });
      }
      //
      uiKey = this.resolveUiKey(uiKey, id);
      dispatch(this.requestEntity(id, uiKey));
      this.getService().getById(id)
      .then(json => {
        dispatch(this.fetchPermissions(id, uiKey, () => {
          dispatch(this.receiveEntity(id, json, uiKey, cb));
        }));
      })
      .catch(error => {
        dispatch(this.receiveError(id, uiKey, error, cb));
      });
    };
  }

  fetchPermissions(id, uiKey = null, cb = null) {
    if (!this.getService().supportsAuthorization()) {
      if (cb) {
        cb();
      }
      return {
        type: EMPTY
      };
    }
    //
    return (dispatch, getState) => {
      if (getState().security.userContext.isExpired) {
        return;
      }
      //
      uiKey = this.resolveUiKey(uiKey, id);
      this.getService().getPermissions(id)
      .then(permissions => {
        dispatch({
          type: RECEIVE_PERMISSIONS,
          id,
          entityType: this.getEntityType(),
          permissions,
          uiKey
        });
        if (cb) {
          cb();
        }
      });
    };
  }

  /**
   * Update entity
   *
   * @param  {object} entity - Entity to update
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @param  {func} cb - function will be called after entity is updated or error occured
   * @return {object} - action
   */
  updateEntity(entity, uiKey = null, cb = null) {
    if (!entity) {
      return {
        type: EMPTY
      };
    }
    uiKey = this.resolveUiKey(uiKey, entity.id);
    return (dispatch) => {
      dispatch(this.requestEntity(entity.id, uiKey));
      this.getService().updateById(entity.id, entity)
      .then(json => {
        dispatch(this.receiveEntity(entity.id, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(entity, uiKey, error, cb));
      });
    };
  }

  /**
   * Patch entity
   *
   * @param  {object} entity - Entity to patch
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @param  {func} cb - function will be called after entity is patched or error occured
   * @return {object} - action
   */
  patchEntity(entity, uiKey = null, cb = null) {
    if (!entity) {
      return {
        type: EMPTY
      };
    }
    uiKey = this.resolveUiKey(uiKey, entity.id);
    return (dispatch) => {
      dispatch(this.requestEntity(entity.id, uiKey));
      this.getService().patchById(entity.id, entity)
      .then(json => {
        dispatch(this.receiveEntity(entity.id, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(entity, uiKey, error, cb));
      });
    };
  }

  /**
   * Create new entity
   *
   * @param  {object} entity - Entity to patch
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @param  {func} cb - function will be called after entity is patched or error occured
   * @return {object} - action
   */
  createEntity(entity, uiKey = null, cb = null) {
    if (!entity) {
      return {
        type: EMPTY
      };
    }
    uiKey = this.resolveUiKey(uiKey, '[new]');
    return (dispatch) => {
      dispatch(this.requestEntity('[new]', uiKey));
      this.getService().create(entity)
      .then(json => {
        dispatch(this.receiveEntity(json.id, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(entity, uiKey, error, cb));
      });
    };
  }

  /**
   * Delete entity
   *
   * @param  {object} entity - Entity to delete
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @param  {func} cb - function will be called after entity is deleted or error occured
   * @return {object} - action
   */
  deleteEntity(entity, uiKey = null, cb = null) {
    if (!entity) {
      return {
        type: EMPTY
      };
    }
    uiKey = this.resolveUiKey(uiKey, entity.id);
    return (dispatch) => {
      dispatch(this.requestEntity(entity.id, uiKey));
      this.getService().deleteById(entity.id)
      .then(() => {
        dispatch(this.deletedEntity(entity.id, entity, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(entity, uiKey, error, cb));
      });
    };
  }

  /**
   * Delete entities - bulk action
   *
   * @param  {array[object]} entities - Entities to delete
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @param  {func} cb - function will be called after entities are deleted or error occured
   * @return {object} - action
   */
  deleteEntities(entities, uiKey = null, cb = null) {
    return (dispatch) => {
      dispatch(
        this.startBulkAction(
          {
            name: 'delete',
            title: this.i18n(`action.delete.header`, { count: entities.length })
          },
          entities.length
        )
      );
      const successEntities = [];
      const approveEntities = [];
      let currentEntity = null; // currentEntity in loop
      entities.reduce((sequence, entity) => {
        return sequence.then(() => {
          // stops when first error occurs
          currentEntity = entity;
          return this.getService().deleteById(entity.id);
        }).then((json) => {
          dispatch(this.updateBulkAction());
          if (json && json.statusCode === 202) {
            approveEntities.push(entity);
          } else {
            successEntities.push(entity);
            // remove entity to redux store
            dispatch(this.deletedEntity(entity.id, entity, uiKey));
          }
        }).catch(error => {
          if (currentEntity.id === entity.id) { // we want show message for entity, when loop stops
            if (!cb) { // if no callback given, we need show error
              dispatch(this.flashMessagesManager.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: this.getNiceLabel(entity) }) }, error));
            } else { // otherwise caller has to show eror etc. himself
              cb(entity, error, null);
            }
          }
          throw error;
        });
      }, Promise.resolve())
      .catch((error) => {
        // nothing - message is propagated before
        // catch is before then - we want execute next then clausule
        return error;
      })
      .then((error) => {
        if (successEntities.length > 0) {
          dispatch(this.flashMessagesManager.addMessage({
            level: 'success',
            message: this.i18n(`action.delete.success`, { count: successEntities.length, records: this.getNiceLabels(successEntities).join(', '), record: this.getNiceLabel(successEntities[0]) })
          }));
        }
        if (approveEntities.length > 0) {
          dispatch(this.flashMessagesManager.addMessage({
            level: 'info',
            message: this.i18n(`action.delete.approve`, { count: approveEntities.length, records: this.getNiceLabels(approveEntities).join(', '), record: this.getNiceLabel(approveEntities[0]) })
          }));
        }
        dispatch(this.stopBulkAction());
        if (cb) {
          cb(null, error, successEntities);
        }
      });
    };
  }

  /**
   * Request entity by id from server
   *
   * @param  {string|number} id    - Entity identifier
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @return {object}              - action
   */
  requestEntity(id, uiKey = null) {
    uiKey = this.resolveUiKey(uiKey, id);
    return {
      type: REQUEST_ENTITY,
      id,
      entityType: this.getEntityType(),
      uiKey
    };
  }

  /**
   * Receive entity and pair it with context. Its pair with uiKey.
   * Used for create new entity in forms.
   *
   * @param  {string|number} id - entity identifier
   * @param  {object} entity - received entity. If entity is null, then will be removed from state
   * @param  {string} uiKey - ui key for loading indicator etc
   * @param  {func} cb - callback after operation
   * @return {object} - action
   */
  receiveEntity(id, entity, uiKey = null, cb = null) {
    if (!id && !entity) { // nothing was recieved
      return {
        type: EMPTY
      };
    }
    //
    if (!id) {
      id = entity.id;
    }
    uiKey = this.resolveUiKey(uiKey, id);
    if (cb) {
      cb(entity, null);
    }
    return (dispatch) => {
      dispatch({
        type: RECEIVE_ENTITY,
        id,
        entityType: this.getEntityType(),
        entity,
        uiKey
      });
      // push entity to store with secondary identifier
      const idAlias = !this.getIdentifierAlias() ? null : entity[this.getIdentifierAlias()];
      if (idAlias) {
        dispatch({
          type: RECEIVE_ENTITY,
          id: idAlias,
          entityType: this.getEntityType(),
          entity,
          uiKey
        });
      }
    };
  }

  /**
   * Receive entity was deleted by id from server
   *
   * @param  {string|number} id - entity identifier
   * @param  {string} uiKey - ui key for loading indicator etc
   * @param  {func} cb - callback after operation
   * @return {object} - action
   */
  deletedEntity(id, entity, uiKey = null, cb = null) {
    if (!id) { // nothing was recieved
      return {
        type: EMPTY
      };
    }
    uiKey = this.resolveUiKey(uiKey, id);
    if (cb) {
      cb(entity, null);
    }
    return {
      type: DELETED_ENTITY,
      id,
      entityType: this.getEntityType(),
      uiKey
    };
  }

  /**
   * Receive error from server call
   *
   * @param  {string|number} id - entity identifier (could be null)
   * @param  {object} entity - received entity
   * @param  {string} uiKey - ui key for loading indicator etc
   * @param  {object} error - received error
   * @return {object} - action
   */
  receiveError(entity, uiKey = null, error = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey, entity ? entity.id : null);
    return (dispatch) => {
      if (cb) {
        cb(null, error);
      } else {
        dispatch(this.flashMessagesManager.addErrorMessage({
          key: 'error-' + this.getEntityType()
        }, error));
      }
      dispatch({
        type: RECEIVE_ERROR,
        id: entity ? entity.id : null,
        uiKey,
        error
      });
    };
  }

  /**
   * Pagination - reuse saved searchparameters in state preserves entered sorts and filters
   *
   * @param  {number} page  - current page (counted from zero)
   * @param  {number} size  - page size
   * @param  {string} uiKey - ui key for loading indicator etc.
   * @return {action}       - action
   */
  handlePagination(page, size, uiKey = null) {
    uiKey = this.resolveUiKey(uiKey);
    return (dispatch, getState) => {
      let searchParameters = this.getSearchParameters(getState().data.ui[uiKey].searchParameters);
      searchParameters = searchParameters.setSize(size).setPage(page);
      dispatch(this.fetchEntities(searchParameters, uiKey));
    };
  }

  /**
  * Data sorting
  */
  handleSort(property, order, uiKey = null) {
    uiKey = this.resolveUiKey(uiKey);
    return (dispatch, getState) => {
      let searchParameters = this.getSearchParameters(getState().data.ui[uiKey].searchParameters);
      searchParameters = searchParameters.clearSort().setSort(property, order !== 'DESC');
      dispatch(this.fetchEntities(searchParameters, uiKey));
    };
  }

  /**
   * Check if is ui key present
   */
  containsUiKey(state, uiKey = null) {
    uiKey = this.resolveUiKey(uiKey);
    if (!state || !state.data || !state.data.ui || !state.data.ui[uiKey] || !state.data.ui[uiKey].items) {
      return false;
    }
    return true;
  }

  /**
   * Read entities associarted by given uiKey items from ui store
   *
   * @param  {state} state [description]
   * @param  {string} uiKey - ui key for loading indicator etc.
   * @return {array[entity]}
   */
  getEntities(state, uiKey = null) {
    return Utils.Ui.getEntities(state, this.resolveUiKey(uiKey));
  }

  /**
   * Returns entity, if entity is contained in applicateion state.
   * Can be used in select state.
   *
   * @param {state} state - application state
   * @param {string|number} id - entity identifier
   * @param {bool} trimmed - trimmed or full entity is needed
   * @return {object} - entity
   */
  getEntity(state, id, trimmed = null) {
    return Utils.Entity.getEntity(state, this.getEntityType(), id, trimmed);
  }

  /**
   *	Returns entities by ids, if entities are contained in applicateion state.
   *
   * @param  {state} state [description]
   * @param  {array[string|number]} ids  entity ids
   * @param {bool} trimmed - trimmed or full entity is needed
   * @return {array[object]}
   */
  getEntitiesByIds(state, ids = [], trimmed = null) {
    return Utils.Entity.getEntitiesByIds(state, this.getEntityType(), ids, trimmed);
  }

  /**
   * Returns true, when loading for given uiKey proceed
   *
   * @param  {state} state - application state
   * @param  {string} uiKey - ui key for loading indicator etc.
   * @param  {string} id - entity identifier
   * @return {boolean} - true, when loading for given uiKey proceed
   */
  isShowLoading(state, uiKey = null, id = null) {
    return Utils.Ui.isShowLoading(state, this.resolveUiKey(uiKey, id));
  }

  /**
   * Returns error asigned to given uiKey or null, if no error is found
   *
   * @param  {state} state - application state
   * @param  {string} uiKey - ui key for loading indicator etc.
   * @return {objest} - error
   */
  getError(state, uiKey = null, id = null) {
    return Utils.Ui.getError(state, this.resolveUiKey(uiKey, id));
  }

  /**
   * Returns true, when entity by given id is not contained in state and loading does not processing
   *
   * @param  {state}  state - application state
   * @param  {string|number} id - entity identifier
   * @param  {string} uiKey - ui key for loading indicator etc.
   * @return {boolean} - true, when entity is not contained in state and loading does not processing
   */
  fetchEntityIsNeeded(state, id, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey. id);
    // entity is saved in state
    if (this.getEntity(state, id)) {
      return false;
    }
    if (this.getError(state, uiKey, id)) {
      return false;
    }
    if (!cb && this.isShowLoading(state, uiKey, id)) { // if callback is given, then loadinig is needed - callback is called after loading
      return false;
    }
    return true;
  }

  /**
   * Loads requested entity by given id, if entity is not in application state and entity loading does not processing
   *
   * @param  {store}  store - application store
   * @param  {string|number} id - entity identifier
   * @param  {string} uiKey - ui key for loading indicator etc.
   */
  fetchEntityIfNeeded(id, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey, id);
    return (dispatch, getState) => {
      if (this.fetchEntityIsNeeded(getState(), id, uiKey, cb)) {
        dispatch(this.fetchEntity(id, uiKey, cb));
      } else if (cb) {
        cb(this.getEntity(getState(), id), null);
      }
    };
  }

  /**
   * Autocomplete requested entity by given id from BE, if entity is not in application state and entity loading does not processing
   *
   * @param  {store}  store - application store
   * @param  {string|number} id - entity identifier
   * @param  {string} uiKey - ui key for loading indicator etc.
   */
  autocompleteEntityIfNeeded(id, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey, id);
    return (dispatch, getState) => {
      if (this.fetchEntityIsNeeded(getState(), id, uiKey, cb)) {
        if (this.supportsAuthorization()) {
          // autocomplete search by id
          const searchParameters = this.getDefaultSearchParameters().setName(SearchParameters.NAME_AUTOCOMPLETE).setFilter('id', id);
          dispatch(this.fetchEntities(searchParameters, uiKey, (json, error) => {
            if (!error) {
              const data = json._embedded[this.getCollectionType()] || [];
              dispatch(this.receiveEntity(id, data.length > 0 ? data[0] : null, uiKey, cb));
            } else {
              dispatch(this.receiveError(id, uiKey, error, cb));
            }
          }));
        } else {
          dispatch(this.fetchEntity(id, uiKey, cb));
        }
      } else if (cb) {
        cb(this.getEntity(getState(), id), null);
      }
    };
  }

  /**
   * Clears all entities by type from application state
   *
   * @param  {string} uiKey - ui key for loading indicator etc.
   * @return {action} - action
   */
  clearEntities(uiKey = null) {
    uiKey = this.resolveUiKey(uiKey);
    return {
      type: CLEAR_ENTITIES,
      entityType: this.getEntityType(),
      uiKey
    };
  }

  /**
   * Start global bulk action
   *
   * @param  {object} bulkAction { name, title }
   * @param  {number} size bulk action size
   * @return {action}  action
   */
  startBulkAction(bulkAction, size) {
    return {
      type: START_BULK_ACTION,
      action: bulkAction,
      size
    };
  }

  /**
   * Update globally processed bulk action
   *
   * @param  {number} counterIncrement counter increment
   * @return {action} action
   */
  updateBulkAction(counterIncrement = 1) {
    return {
      type: PROCESS_BULK_ACTION,
      counterIncrement
    };
  }

  /**
   * Stop global bulk action
   *
   * @return {action} action
   */
  stopBulkAction() {
    return {
      type: STOP_BULK_ACTION
    };
  }
  /**
   * Load revisions from server
   *
   * @param entityId {string, number}
   * @return {object} - action
   */
  fetchRevisions(entityId, uiKey = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getRevisions(entityId)
        .then(json => {
          dispatch(this.dataManager.receiveData(uiKey, json));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Load single revision from server
   *
   * @param entityId {string, number}
   * @param revId {number}
   * @return {object} - action
   */
  fetchRevision(entityId, revId, uiKey = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getRevision(entityId, revId)
        .then(json => {
          dispatch(this.dataManager.receiveData(uiKey, json));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * What logged user can do with ui key and underlying entity.
   *
   * @param  {state} state - application state
   * @param  {string} uiKey - ui key for loading indicator etc.
   * @param  {string} id - entity identifier
   * @return {arrayOf(authority)} what logged user can do with ui key and underlying entity
   */
  getPermissions(state, uiKey = null, id = null) {
    return Utils.Permission.getPermissions(state, this.resolveUiKey(uiKey, id));
  }

  /**
   * Authorization evaluator helper - evaluates save permission on given entity
   *
   * If entity is null - CREATE is evaluated
   *
   * @param  {object} entity
   * @param  {arrayOf(string)} permissions
   * @return {bool}
   */
  canSave(entity = null, permissions = null) {
    if (!this.getGroupPermission()) {
      return false;
    }
    if (Utils.Entity.isNew(entity)) {
      return SecurityManager.hasAuthority(`${this.getGroupPermission()}_CREATE`);
    }
    return (!this.supportsAuthorization() || Utils.Permission.hasPermission(permissions, 'UPDATE')) && SecurityManager.hasAuthority(`${this.getGroupPermission()}_UPDATE`);
  }

  /**
   * Authorization evaluator helper - evaluates delete permission on given entity
   *
   * @param  {object} entity
   * @param  {arrayOf(string)} permissions
   * @return {bool}
   */
  canDelete(entity = null, permissions = null) {
    if (!this.getGroupPermission()) {
      return false;
    }
    if (!this.supportsAuthorization() || !entity) {
      return SecurityManager.hasAuthority(`${this.getGroupPermission()}_DELETE`);
    }
    return Utils.Permission.hasPermission(permissions, 'DELETE') && SecurityManager.hasAuthority(`${this.getGroupPermission()}_DELETE`);
  }
}
