import { ExportImportService } from '../../services';
import DataManager from './DataManager';
import EntityManager from './EntityManager';

/**
 * ExportImport manager
 *
 * @author Vít Švanda
 */
export default class ExportImportManager extends EntityManager {

  constructor() {
    super();
    this.service = new ExportImportService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  /**
   * Controlled entity
   */
  getEntityType() {
    return 'ExportImport';
  }

  /**
   * Collection name in search / find response
   */
  getCollectionType() {
    return 'exports';
  }

  execute(entityId, dryRun, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey, entityId);
    return (dispatch) => {
      dispatch(this.requestEntity(entityId, uiKey));
      this.getService().execute(entityId, dryRun)
        .then(json => {
          dispatch(this.receiveEntity(entityId, json, uiKey, cb));
        })
        .catch(error => {
          dispatch(this.receiveError(entityId, uiKey, error, cb));
        });
    };
  }
}
