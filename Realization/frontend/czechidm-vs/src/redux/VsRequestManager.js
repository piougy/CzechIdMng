import { Managers } from 'czechidm-core';
import { VsRequestService } from '../services';

/**
 * Manager controlls request for virtual systems
 *
 * @author Vít Švanda
 */
export default class VsRequestManager extends Managers.EntityManager {

  constructor() {
    super();
    this.service = new VsRequestService();
  }

  getModule() {
    return 'vs';
  }

  getService() {
    return this.service;
  }

  /**
   * Controlled entity
   */
  getEntityType() {
    return 'VsRequest';
  }

  /**
   * Collection name in search / find response
   */
  getCollectionType() {
    return 'requests';
  }

  /**
  * Mark virtual system request as realized (changes will be propagated to VsAccount)
  */
  realize(entityId, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey, entityId);
    return (dispatch) => {
      dispatch(this.requestEntity(entityId, uiKey));
      this.getService().realize(entityId)
      .then(json => {
        dispatch(this.receiveEntity(entityId, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(entityId, uiKey, error, cb));
      });
    };
  }

  /**
  * Cancel virtual system request
  */
  cancel(entityId, reason, uiKey = null, cb = null) {
    uiKey = this.resolveUiKey(uiKey, entityId);
    return (dispatch) => {
      dispatch(this.requestEntity(entityId, uiKey));
      this.getService().cancel(entityId, reason)
      .then(json => {
        dispatch(this.receiveEntity(entityId, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(entityId, uiKey, error, cb));
      });
    };
  }

  /**
  * Mark virtual system request as realized (changes will be propagated to VsAccount)
  */
  realizeUi(bulkActionValue, ids, manager, event) {
    if (event) {
      event.preventDefault();
    }
    const selectedEntities = manager.getEntitiesByIds(this.context.store.getState(), ids);
    this.refs[`confirm-realize`].show(
      this.i18n(`vs:content.vs-requests.action.realize.message`, { count: selectedEntities.length, record: manager.getNiceLabel(selectedEntities[0]), records: manager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`vs:content.vs-requests.action.realize.header`, { count: selectedEntities.length, records: manager.getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      const cb = (realizedEntity, newError) => {
        if (!newError) {
          this.setState({
            showLoading: false
          });
          if (this.refs.table) {
            this.refs.table.getWrappedInstance().reload();
          }
          this.addMessage({ message: this.i18n('vs:content.vs-requests.action.realize.success', { record: realizedEntity.uid }) });
        } else {
          this.setState({
            showLoading: false
          });
          this.addError(newError);
          if (this.refs.table) {
            this.refs.table.getWrappedInstance().reload();
          }
        }
      };

      for (const id of ids) {
        this.setState({
          showLoading: true
        });
        this.context.store.dispatch(manager.realize(id, null, cb));
      }
    }, () => {
      // Rejected
    });
    return;
  }

  /**
  * Cancel virtual system request
  */
  cancelUi(bulkActionValue, ids, manager, event) {
    if (event) {
      event.preventDefault();
    }
    const selectedEntities = manager.getEntitiesByIds(this.context.store.getState(), ids);
    this.refs[`confirm-cancel`].show(
      this.i18n(`vs:content.vs-requests.action.cancel.message`, { count: selectedEntities.length, record: manager.getNiceLabel(selectedEntities[0]), records: manager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`vs:content.vs-requests.action.cancel.header`, { count: selectedEntities.length, records: manager.getNiceLabels(selectedEntities).join(', ') }),
      manager._validateCancelDialog.bind(this)
    ).then(() => {
      const cb = (realizedEntity, newError) => {
        if (!newError) {
          this.setState({
            showLoading: false
          });
          if (this.refs.table) {
            this.refs.table.getWrappedInstance().reload();
          }
          this.addMessage({ message: this.i18n('vs:content.vs-requests.action.cancel.success', { record: realizedEntity.uid }) });
        } else {
          this.setState({
            showLoading: false
          });
          this.addError(newError);
          if (this.refs.table) {
            this.refs.table.getWrappedInstance().reload();
          }
        }
      };

      for (const id of ids) {
        this.setState({
          showLoading: true
        });
        const reason = this.refs['cancel-form'].getData()['cancel-reason'];
        this.context.store.dispatch(manager.cancel(id, reason, null, cb));
      }
    }, () => {
      // Rejected
    });
    return;
  }

  _validateCancelDialog(result) {
    if (result === 'reject') {
      return true;
    }
    if (result === 'confirm' && this.refs['cancel-form'].isFormValid()) {
      return true;
    }
    return false;
  }
}
