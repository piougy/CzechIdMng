import Immutable from 'immutable';
//
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

  /**
   * Load connector configuration for given system
   *
   * @param  {string} id system identifier
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
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

  /**
   * Saves connector configuration form values
   *
   * @param  {string} id system identifier
   * @param  {arrayOf(entity)} values filled form values
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
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

  /**
   *  Fetch all available framworks and their connectors and put them to redux data
   *
   * @return {action}
   */
  fetchAvailableFrameworks() {
    const uiKey = SystemManager.AVAILABLE_CONNECTORS;
    //
    return (dispatch, getState) => {
      let availableFrameworks = Managers.DataManager.getData(getState(), uiKey);
      if (availableFrameworks) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getAvailableConnectors()
          .then(json => {
            availableFrameworks = new Immutable.Map();
            for (const framework in json) {
              if (!json.hasOwnProperty(framework)) {
                continue;
              }
              let availableConnectors = new Immutable.Map();
              json[framework].forEach(connector => {
                availableConnectors = availableConnectors.set(connector.connectorKey.fullName, connector);
              });
              availableFrameworks = availableFrameworks.set(framework, availableConnectors);
            }
            dispatch(this.dataManager.receiveData(uiKey, availableFrameworks));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.receiveError(null, uiKey, error));
          });
      }
    };
  }
}

SystemManager.AVAILABLE_CONNECTORS = 'connectors-available';
