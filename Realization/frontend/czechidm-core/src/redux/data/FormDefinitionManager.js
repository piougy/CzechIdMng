import EntityManager from './EntityManager';
import { FormDefinitionService } from '../../services';
import DataManager from './DataManager';

export default class FormDefinitionManager extends EntityManager {

  constructor() {
    super();
    this.service = new FormDefinitionService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'FormDefinition';
  }

  getCollectionType() {
    return 'formDefinitions';
  }

  /**
   * Return search paramaters for endpoind with information about form definition types.
   */
  getDefinitionTypesSearchParameters() {
    return this.getService().getDefinitionTypesSearchParameters();
  }

  fetchTypes(uiKey = null, cb = null) {
    return (dispatch) => {
      dispatch(this.requestEntities(null, uiKey));
      this.getService().getTypes()
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
