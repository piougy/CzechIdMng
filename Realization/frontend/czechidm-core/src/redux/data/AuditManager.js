import EntityManager from './EntityManager';
import { AuditService } from '../../services';
import DataManager from './DataManager';

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
   * Return seearch paramaters for endpoind with information about audited entities.
   */
  getAuditedEntitiesNames() {
    return this.getService().getAuditedEntitiesSearchParameters();
  }

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
}
