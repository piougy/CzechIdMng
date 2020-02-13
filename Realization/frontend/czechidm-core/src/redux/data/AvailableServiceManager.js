import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import {AvailableServiceService} from '../../services';
import DataManager from './DataManager';

/**
 * Manages service agenda of all services available in scripts
 *
 * @author Ondrej Husnik
 */
export default class AvailableServiceManager extends EntityManager {

  constructor() {
    super();
    this.service = new AvailableServiceService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'AvailableService';
  }

  getCollectionType() {
    return 'availableServices';
  }

  fetchAvailableServices() {
    const uiKey = AvailableServiceManager.UI_KEY_AVAILABLE_SERVICES;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getAvailableServices()
        .then(json => {
          let availableServices = new Immutable.Map();
          json._embedded.availableServices.forEach(availableService => {
            availableServices = availableServices.set(availableService.id, availableService);
          });
          dispatch(this.dataManager.receiveData(uiKey, availableServices));
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }
}

AvailableServiceManager.UI_KEY_AVAILABLE_SERVICES = 'registered-available-service';
