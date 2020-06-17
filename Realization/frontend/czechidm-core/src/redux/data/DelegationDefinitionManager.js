import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import { DelegationDefinitionService } from '../../services';
import * as Utils from '../../utils';
import DataManager from './DataManager';
import IdentityManager from './IdentityManager';

const identityManager = new IdentityManager();

/**
 * Delegation definition manager.
 *
 * @author Vít Švanda
 * @since 10.4.0
 */
export default class DelegationDefinitionManager extends EntityManager {

  constructor() {
    super();
    this.service = new DelegationDefinitionService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'DelegationDefinition';
  }

  getCollectionType() {
    return 'delegationDefs';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded || !entity._embedded.delegator) {
      return null;
    }
    return `${this._getIdentityName(entity._embedded.delegator)} 🡆 ${this._getIdentityName(entity._embedded.delegate)}`;
  }

  _getIdentityName(identity) {
    const fullName = identityManager.getFullName(identity);
    if (fullName) {
      return fullName;
    }
    return identityManager.getNiceLabel(identity);
  }

  canSave(definition, _permissions) {
    // The delegation cannot be updated!
    if (definition && !Utils.Entity.isNew(definition)) {
      return false;
    }

    return super.canSave(definition, _permissions);
  }


  /**
   * Loads all registered delegation types.
   *
   * @return {action}
   */
  fetchSupportedTypes() {
    const uiKey = DelegationDefinitionManager.UI_KEY_SUPPORTED_TYPES;
    //
    return (dispatch, getState) => {
      const loaded = DataManager.getData(getState(), uiKey);
      if (loaded) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getSupportedTypes()
          .then(json => {
            let types = new Immutable.Map();
            if (json._embedded && json._embedded.delegationTypes) {
              json._embedded.delegationTypes.forEach(item => {
                types = types.set(item.id, item);
              });
            }
            dispatch(this.dataManager.receiveData(uiKey, types));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.dataManager.receiveError(null, uiKey, error));
          });
      }
    };
  }
}

DelegationDefinitionManager.UI_KEY_SUPPORTED_TYPES = 'delegation-supported-types';
