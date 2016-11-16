import { Managers, Domain } from 'czechidm-core';
import { SystemService } from '../services';

export default class SystemManager extends Managers.EntityManager {

  constructor() {
    super();
    this.service = new SystemService();
    this.dataManager = new Managers.DataManager();
  }

  getModule() {
    return 'acc';
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'System';
  }

  getCollectionType() {
    return 'systems';
  }

  fetchConnectorConfiguration(id, uiKey, cb = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));

      const connectorFormDefinitionPromise = this.getService().getConnectorFormDefinition(id);
      const connectorFormValuesPromise = this.getService().getConnectorFormValues(id);

      Promise.all([connectorFormDefinitionPromise, connectorFormValuesPromise])
        .then((jsons) => {
          const formDefinition = jsons[0];
          const formValues = jsons[1]._embedded.sysSystemFormValues;

          const formInstance = new Domain.FormInstance(formDefinition, formValues);

          dispatch(this.dataManager.receiveData(uiKey, formInstance));
          if (cb) {
            cb(formInstance);
          }
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }

  saveConnectorConfiguration(id, values, uiKey, cb = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().saveConnectorFormValues(id, values)
      .then(() => {
        dispatch(this.fetchConnectorConfiguration(id, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(null, uiKey, error, cb));
      });
    };
  }
}
