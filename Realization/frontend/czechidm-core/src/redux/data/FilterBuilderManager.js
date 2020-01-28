import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import {FilterBuilderService} from '../../services';
import DataManager from './DataManager';

/**
 * Provides information about filter builders from backend and their administrative methods.
 *
 * @author Kolychev Artem
 * @author Radek Tomiška
 * @since 9.7.7
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
    return 'FilterBuilder';
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
          json._embedded[this.getCollectionType()].forEach(filterBuilder => {
            filterBuilders = filterBuilders.set(filterBuilder.id, filterBuilder);
          });
          dispatch(this.dataManager.receiveData(uiKey, filterBuilders));
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Activate filter.
   *
   * @param {string} filterBuilderId
   * @param {func} cb
   */
  setEnabled(filterBuilderId, cb = null) {
    if (!filterBuilderId) {
      return null;
    }
    return (dispatch) => {
      this.getService().setEnabled(filterBuilderId)
        .then(() => {
          // reload is needed => other filter builder is not active, after activate other one
          dispatch(this.fetchRegisteredFilterBuilders());
          if (cb) {
            cb(null, null);
          }
        })
        .catch(error => {
          dispatch(this.receiveError({ id: filterBuilderId }, FilterBuildersManager.UI_KEY_FILTER_BUILDERS, error, cb));
        });
    };
  }
}

FilterBuildersManager.UI_KEY_FILTER_BUILDERS = 'registered-filter-builders';
