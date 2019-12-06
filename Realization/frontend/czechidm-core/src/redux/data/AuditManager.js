import EntityManager from './EntityManager';
import { AuditService } from '../../services';
import DataManager from './DataManager';
import * as Utils from '../../utils';

/**
 * Uikey can be for all details same, beacuse result is also same.
 */
const AUDIT_ENTITY_NAMES_UIKEY = 'audit-entity-names-uikey';

export default class AuditManager extends EntityManager {

  constructor() {
    super();
    this.service = new AuditService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Audit';
  }

  getCollectionType() {
    return 'audits';
  }

  /**
   * Return search paramaters for endpoind with information about audited entities.
   */
  getSearchParametersAuditedEntitiesNames() {
    return this.getService().getAuditedEntitiesSearchParameters();
  }

  /**
   * Return differen between two revision
   */
  fetchDiffBetweenVersion(firstRevId, secondRevId, uiKey = null, cb = null) {
    return (dispatch) => {
      dispatch(this.requestEntities(null, uiKey));
      this.getService().getDiffBetweenVersion(firstRevId, secondRevId)
      .then(json => {
        if (cb) {
          cb(json, null);
        }
        dispatch(this.dataManager.receiveData(uiKey, json));
      })
      .catch(error => {
        dispatch(this.receiveError({}, uiKey, error, cb));
      });
    };
  }

  /**
   * Return revious version of revision
   */
  fetchPreviousVersion(revId, uiKey = null, cb = null) {
    return (dispatch) => {
      dispatch(this.requestEntities(null, uiKey));
      this.getService().getPreviousVersion(revId)
      .then(json => {
        if (cb) {
          cb(json, null);
        }
        dispatch(this.dataManager.receiveData(uiKey, json));
      })
      .catch(error => {
        dispatch(this.receiveError({}, uiKey, error, cb));
      });
    };
  }

  /**
   * Fetch audited entity names
   */
  fetchAuditedEntitiesNames(cb = null) {
    return (dispatch) => {
      const searchParameters = this.getSearchParametersAuditedEntitiesNames();
      const uiKey = AUDIT_ENTITY_NAMES_UIKEY;
      dispatch(this.requestEntities(searchParameters, uiKey));
      this.getService().search(searchParameters)
      .then(json => {
        dispatch(this.dataManager.receiveData(uiKey, json, cb));
      })
      .catch(error => {
        dispatch(this.receiveError({}, uiKey, error, cb));
      });
    };
  }

  /**
   * Get loaded audit entities names
   */
  getAuditedEntitiesNames(state) {
    return DataManager.getData(state, AUDIT_ENTITY_NAMES_UIKEY);
  }

  prepareOptionsFromAuditedEntitiesNames(entities) {
    if (entities !== null) {
      return entities._embedded.strings.map(item => {
        return {
          value: item.content,
          niceLabel: Utils.Ui.getSimpleJavaType(item.content) // simple name only - unique entity name is required by dev stack (~hibernate).
        };
      });
    }
  }
}
