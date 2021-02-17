import Immutable from 'immutable';
//
import { Managers } from 'czechidm-core';
import { RemoteServerService } from '../services';


/**
 * Remote server with connectors.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
export default class RemoteServerManager extends Managers.EntityManager {

  constructor() {
    super();
    this.service = new RemoteServerService();
  }

  getModule() {
    return 'acc';
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'RemoteServer';
  }

  getCollectionType() {
    return 'connectorServers';
  }

  /**
   * Returns available frameworks with connectors on remote server.
   *
   * @param  {string} remoteServerId remote server identifier
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  fetchAvailableFrameworks(remoteServerId, uiKey, cb = null) {
    return (dispatch) => {
      let availableFrameworks = new Immutable.Map();
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().fetchAvailableFrameworks(remoteServerId)
        .then(json => {
          for (const framework in json) {
            if (!json.hasOwnProperty(framework)) {
              continue;
            }
            let availableConnectors = new Immutable.Map();
            if (json[framework] == null) {
              continue;
            }
            json[framework].forEach(connector => {
              availableConnectors = availableConnectors.set(connector.connectorKey.fullName, connector);
            });
            availableFrameworks = availableFrameworks.set(framework, availableConnectors);
          }
          dispatch(this.dataManager.receiveData(uiKey, availableFrameworks, cb));
        })
        .catch(error => {
          if (cb) {
            cb(null, error);
          }
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Loads all registered connector types.
   *
   * @param  {string} remoteServerId remote server identifier
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  fetchConnectorTypes(remoteServerId, uiKey, cb = null) {
    return (dispatch, getState) => {
      const loaded = Managers.DataManager.getData(getState(), uiKey);
      if (loaded) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.getDataManager().requestData(uiKey));
        this.getService().getSupportedTypes()
          .then(json => {
            let types = new Immutable.Map();
            if (json._embedded && json._embedded.connectorTypes) {
              json._embedded.connectorTypes.forEach(item => {
                types = types.set(item.id, item);
              });
            }
            dispatch(this.getDataManager().receiveData(uiKey, types, cb));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.getDataManager().receiveError(null, uiKey, error));
          });
      }
    };
  }
}
