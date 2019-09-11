import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import {FilterBuilderService} from '../../services';
import DataManager from './DataManager';

/**
 * Provides information about filter builders from backend and their administrative methods.
 */
export default class FilterBuildersManager extends EntityManager {

  constructor() {
    super();
    this.service = new FilterBuilderService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'FilterBuilders';
  }

  getCollectionType() {
    return 'filterBuilders';
  }

  fetchRegisteredFilterBuilders() {
    const uiKey = FilterBuildersManager.UI_KEY_FILTER_BUILDERS;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getRegisteredFilterBuilders()
        .then(json => {
          let filterBuilders = new Immutable.Map();
          json._embedded.filterBuilders.forEach(filterBuilder => {
            filterBuilders = filterBuilders.set(filterBuilder.name, filterBuilder);
          });
          dispatch(this.dataManager.receiveData(uiKey, filterBuilders));
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }
}

FilterBuildersManager.UI_KEY_FILTER_BUILDERS = 'registered-filter-builders';
