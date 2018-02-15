import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import { EntityEventProcessorService } from '../../services';
import DataManager from './DataManager';

/**
 * Provides informations  about entity event processors from backend and their administrative methods.
 */
export default class EntityEventProcessorManager extends EntityManager {

  constructor() {
    super();
    this.service = new EntityEventProcessorService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'EntityEventProcessor';
  }

  getCollectionType() {
    return 'entityEventProcessors';
  }

  fetchRegisteredProcessors() {
    const uiKey = EntityEventProcessorManager.UI_KEY_PROCESSORS;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getReqisteredProcessors()
        .then(json => {
          let registeredProcessors = new Immutable.Map();
          json._embedded.entityEventProcessors.forEach(item => {
            registeredProcessors = registeredProcessors.set(item.id, item);
          });
          dispatch(this.dataManager.receiveData(uiKey, registeredProcessors));
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Set processor endabled / disabled.
   *
   * @param {string} processorId
   * @param {boolean} enable
   * @param {func} cb
   */
  setEnabled(processorId, enable = true, cb = null) {
    if (!processorId) {
      return null;
    }
    const uiKey = EntityEventProcessorManager.UI_KEY_MODULES;
    return (dispatch, getState) => {
      dispatch(this.requestEntity(processorId, uiKey));
      this.getService().setEnabled(processorId, enable)
      .then(json => {
        const registeredProcessors = DataManager.getData(getState(), EntityEventProcessorManager.UI_KEY_MODULES);
        if (registeredProcessors.has(json.id)) {
          registeredProcessors.get(json.id).disabled = json.disabled;
        }
        dispatch(this.dataManager.receiveData(uiKey, registeredProcessors));
        if (cb) {
          cb(json, null);
        }
      })
      .catch(error => {
        dispatch(this.receiveError({ id: processorId, disabled: !enable }, uiKey, error, cb));
      });
    };
  }
}

EntityEventProcessorManager.UI_KEY_PROCESSORS = 'registered-entity-event-processors';
