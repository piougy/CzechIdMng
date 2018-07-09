import Immutable from 'immutable';
import EntityManager from './EntityManager';
import { FormDefinitionAttributesValuesService } from '../../services';
import DataManager from './DataManager';

/**
 * @author Roman Kučera
 */
export default class FormDefinitionAttributesValuesManager extends EntityManager {

  constructor() {
    super();
    this.service = new FormDefinitionAttributesValuesService();
    this.dataManager = new DataManager();
  }


  getService() {
    return this.service;
  }

  getEntityType() {
    return 'FormValue';
  }

  getCollectionType() {
    return 'formValues';
  }

  fetchAttributesValues(uiKey = null, cb = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getAttributesValues()
        .then(json => {
          if (cb) {
            cb(json, null);
          }
          let attributesValues = new Immutable.Map();
          console.log('json ' + json);
          json._embedded.formValues.forEach(item => {
            attributesValues = attributesValues.set(item.id, item);
            console.log('item ' + item);
          });
          dispatch(this.dataManager.receiveData(uiKey, attributesValues));
        })
        .catch(error => {
          dispatch(this.receiveError({}, uiKey, error, cb));
        });
    };
  }
}
