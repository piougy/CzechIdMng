import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import { FormProjectionService } from '../../services';
import FormDefinitionManager from './FormDefinitionManager';
import DataManager from './DataManager';

/**
 * Form projections.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
export default class FormProjectionManager extends EntityManager {

  constructor() {
    super();
    this.service = new FormProjectionService();
    this.formDefinitionManager = new FormDefinitionManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'FormProjection';
  }

  getCollectionType() {
    return 'formProjections';
  }

  /**
   * Extended nice label.
   *
   * @param  {entity} entity
   * @param  {boolean} showOwnerType show owner type
   * @return {string}
   */
  getNiceLabel(entity, showOwnerType = true) {
    return this.getService().getNiceLabel(entity, showOwnerType);
  }

  /**
   * Projection property localization.
   *
   * @param  {object} projection
   * @param  {string} property
   * @param  {string} [defaultValue=null]
   * @return {string}
   */
  getLocalization(projection, property, defaultValue = null) {
    // reuse eav form feature
    return this.formDefinitionManager.getLocalization({ ...projection, type: 'form-projection' }, property, defaultValue);
  }

  /**
   * Loads all registered routes (available for form projections)
   *
   * @return {action}
   */
  fetchSupportedRoutes() {
    const uiKey = FormProjectionManager.UI_KEY_SUPPORTED_ROUTES;
    //
    return (dispatch, getState) => {
      const loaded = DataManager.getData(getState(), uiKey);
      if (loaded) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getSupportedRoutes()
          .then(json => {
            let routes = new Immutable.Map();
            if (json._embedded && json._embedded.formProjectionRoutes) {
              json._embedded.formProjectionRoutes.forEach(item => {
                routes = routes.set(item.id, item);
              });
            }
            dispatch(this.dataManager.receiveData(uiKey, routes));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.dataManager.receiveError(null, uiKey, error));
          });
      }
    };
  }
}

FormProjectionManager.UI_KEY_SUPPORTED_ROUTES = 'form-projection-supported-routes';
