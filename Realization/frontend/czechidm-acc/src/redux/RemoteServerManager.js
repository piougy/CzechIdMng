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
          if (cb) {
            cb(availableFrameworks);
          }
          dispatch(this.dataManager.receiveData(uiKey, availableFrameworks));
        })
        .catch(error => {
          if (cb) {
            cb(null, error);
          }
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }
}
