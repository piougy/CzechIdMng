import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import { GeneratedValueService } from '../../services';
import DataManager from './DataManager';

/**
 * Manager for Generated attributes
 *
 * @author Ondřej Kopr
 */
export default class GeneratedValueManager extends EntityManager {

  constructor() {
    super();
    this.service = new GeneratedValueService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'GeneratedValue';
  }

  getCollectionType() {
    return 'generatedValues';
  }

  getGroupPermission() {
    return 'GENERATEDVALUE';
  }

  /**
   * Loads all entities wich supports generating
   *
   * @return {action}
   */
  fetchSupportedEntities() {
    const uiKey = GeneratedValueManager.UI_KEY_SUPPORTED_ENTITIES;
    //
    return (dispatch, getState) => {
      const loaded = DataManager.getData(getState(), uiKey);
      if (loaded) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getSupportedEntities()
          .then(json => {
            dispatch(this.dataManager.receiveData(uiKey, json));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.dataManager.receiveError(null, uiKey, error));
          });
      }
    };
  }

  /**
   * Loads all available generators
   *
   * @return {action}
   */
  fetchAvailableGenerators(entityType = null) {
    const uiKey = GeneratedValueManager.UI_KEY_AVAILABLE_GENERATORS;
    //
    return (dispatch, getState) => {
      const loaded = DataManager.getData(getState(), uiKey);
      if (loaded) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getAvailableGenerators(entityType)
          .then(json => {
            let generators = new Immutable.Map();
            if (json._embedded && json._embedded.generatorDefinitions) {
              json._embedded.generatorDefinitions.forEach(item => {
                generators = generators.set(item.generatorType, item);
              });
            }
            dispatch(this.dataManager.receiveData(uiKey, generators));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.dataManager.receiveError(null, uiKey, error));
          });
      }
    };
  }

  /**
   * Return localization prefix for generator. Last character in string is dot.
   */
  getLocalizationPrefixForGenerator() {
    return 'eav.value-generator.';
  }
}

GeneratedValueManager.UI_KEY_SUPPORTED_ENTITIES = 'generated-attr-supported-entities';
GeneratedValueManager.UI_KEY_AVAILABLE_GENERATORS = 'generated-attr-available-generators';
