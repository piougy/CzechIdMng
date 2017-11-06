import { Managers } from 'czechidm-core';
import { SystemEntityService } from '../services';

const service = new SystemEntityService();

export default class SystemEntityManager extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'SystemEntity'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'systemEntities';
  }

  fetchConnectorObject(entityId, uiKey = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getConnectorObject(entityId)
        .then(json => {
          dispatch(this.dataManager.receiveData(uiKey, json));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }
}
