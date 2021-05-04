import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import { BulkActionService } from '../../services';
import DataManager from './DataManager';

/**
 * Provides information about bulk actions from backend and their administrative methods.
 *
 * @author Radek Tomiška
 * @since 10.6.0
 */
export default class BulkActionManager extends EntityManager {

  constructor() {
    super();
    this.service = new BulkActionService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'BulkActione';
  }

  getCollectionType() {
    return 'bulkOperations';
  }

  fetchRegisteredBulkActions() {
    const uiKey = BulkActionManager.UI_KEY_BULK_ACTIONS;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getRegisteredBulkActions()
        .then(json => {
          let bulkActions = new Immutable.Map();
          json._embedded[this.getCollectionType()].forEach(bulkAction => {
            bulkActions = bulkActions.set(bulkAction.id, bulkAction);
          });
          dispatch(this.dataManager.receiveData(uiKey, bulkActions));
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Set bulk action endabled / disabled.
   *
   * @param {string} bulkActionId
   * @param {boolean} enable
   * @param {func} cb
   */
  setEnabled(bulkActionId, enable = true, cb = null) {
    if (!bulkActionId) {
      return null;
    }
    const uiKey = BulkActionManager.UI_KEY_BULK_ACTIONS;
    return (dispatch, getState) => {
      dispatch(this.requestEntity(bulkActionId, uiKey));
      this.getService().setEnabled(bulkActionId, enable)
        .then(json => {
          const registeredBulkActions = DataManager.getData(getState(), BulkActionManager.UI_KEY_BULK_ACTIONS);
          if (registeredBulkActions.has(json.id)) {
            registeredBulkActions.get(json.id).disabled = json.disabled;
          }
          dispatch(this.dataManager.receiveData(uiKey, registeredBulkActions));
          // reload all bulk actions
          dispatch(this.clearAllBulkActions());
          //
          if (cb) {
            cb(json, null);
          }
        })
        .catch(error => {
          dispatch(this.receiveError({ id: bulkActionId, disabled: !enable }, uiKey, error, cb));
        });
    };
  }
}

BulkActionManager.UI_KEY_BULK_ACTIONS = 'registered-bulk-actions';
