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
  _realize(entityId, uiKey = null, cb = null) {
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
  * Mark virtual system request as realized (changes will be propagated to VsAccount)
  */
  realize(bulkActionValue, ids, detail, callback, event) {
    if (event) {
      event.preventDefault();
    }
    const dispatch = detail.context.store.dispatch;
    const selectedEntities = this.getEntitiesByIds(detail.context.store.getState(), ids);
    detail.refs[`confirm-realize`].show(
      detail.i18n(`vs:content.vs-requests.action.realize.message`, { count: selectedEntities.length, record: this.getNiceLabel(selectedEntities[0]), records: this.getNiceLabels(selectedEntities).join(', ') }),
      detail.i18n(`vs:content.vs-requests.action.realize.header`, { count: selectedEntities.length, records: this.getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      const cb = (realizedEntity, newError) => {
        if (!newError) {
          detail.setState({
            showLoading: false
          });
          if (detail.refs.table) {
            detail.refs.table.getWrappedInstance().reload();
          }
          if (callback) {
            callback();
          }
          detail.addMessage({ message: detail.i18n('vs:content.vs-requests.action.realize.success', { count: 1, record: realizedEntity.uid }) });
        } else {
          detail.setState({
            showLoading: false
          });
          detail.addError(newError);
          if (detail.refs.table) {
            detail.refs.table.getWrappedInstance().reload();
          }
        }
      };

      for (const id of ids) {
        detail.setState({
          showLoading: true
        });
        dispatch(this._realize(id, null, cb));
      }
    }, () => {
      // Rejected
    });
    return;
  }

  /**
  * Cancel virtual system request
  */
  cancel(bulkActionValue, ids, detail, callback, event) {
    if (event) {
      event.preventDefault();
    }
    const dispatch = detail.context.store.dispatch;
    const selectedEntities = this.getEntitiesByIds(detail.context.store.getState(), ids);
    detail.refs[`confirm-cancel`].show(
      detail.i18n(`vs:content.vs-requests.action.cancel.message`, { count: selectedEntities.length, record: this.getNiceLabel(selectedEntities[0]), records: this.getNiceLabels(selectedEntities).join(', ') }),
      detail.i18n(`vs:content.vs-requests.action.cancel.header`, { count: selectedEntities.length, records: this.getNiceLabels(selectedEntities).join(', ') }),
      this._validateCancelDialog.bind(detail)
    ).then(() => {
      const cb = (realizedEntity, newError) => {
        if (!newError) {
          detail.setState({
            showLoading: false
          });
          if (detail.refs.table) {
            detail.refs.table.getWrappedInstance().reload();
          }
          if (callback) {
            callback();
          }
          dispatch(this.updateBulkAction());
        } else {
          detail.setState({
            showLoading: false
          });
          detail.addError(newError);
          if (detail.refs.table) {
            detail.refs.table.getWrappedInstance().reload();
          }
        }
      };

      if (ids.length !== 1) {
        dispatch(this.startBulkAction(
          {
            name: 'cancel',
            title: detail.i18n(`action.cancel.header`, { count: ids.length })
          },
          ids.length
          )
        );
      }
      const reason = detail.refs['cancel-form'].getData()['cancel-reason'];
      detail.setState({
        showLoading: true
      });
      ids.reduce((sequence, entityId) => {
        const uiKey = this.resolveUiKey(uiKey, entityId);
        return sequence.then(() => {
          dispatch(this.requestEntity(entityId, uiKey));
          // Call backend API
          return this.getService().cancel(entityId, reason);
        }).then(json => {
          dispatch(this.receiveEntity(entityId, json, uiKey, cb));
        })
          .catch(error => {
            dispatch(this.receiveError(entityId, uiKey, error, cb));
          });
      }, Promise.resolve())
      .catch((error) => {
        // nothing - message is propagated before
        // catch is before then - we want execute next then clausule
        return error;
      })
      .then(() => {
        if (ids.length !== 1) {
          dispatch(this.stopBulkAction());
        } else {
          detail.addMessage({ message: detail.i18n('vs:content.vs-requests.action.cancel.success', {
            count: 1,
            record: this.getNiceLabel(this.getEntity(detail.context.store.getState(), ids[0])) } )
          });
        }
      });
    }, () => {
      // Rejected
    });
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
