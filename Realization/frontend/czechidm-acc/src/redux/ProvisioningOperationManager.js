import { Managers } from 'czechidm-core';
import { ProvisioningOperationService } from '../services';

const service = new ProvisioningOperationService();

export default class ProvisioningOperationManager extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'ProvisioningOperation'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'provisioningOperations';
  }

  retry(ids, bulkActionName, cb = null) {
    return (dispatch, getState) => {
      dispatch(
        this.startBulkAction(
          {
            name: bulkActionName,
            title: this.i18n(`acc:content.provisioningOperations.action.${bulkActionName}.header`, { count: ids.length })
          },
          ids.length
        )
      );
      const successNames = [];
      const successIds = [];
      ids.reduce((sequence, operationId) => {
        return sequence.then(() => {
          return this.getService().retry(operationId, bulkActionName);
        }).then(json => {
          dispatch(this.updateBulkAction());
          successIds.push(operationId);
          successNames.push(this.getNiceLabel(this.getEntity(getState(), operationId)));
          // new entity to redux trimmed store
          dispatch(this.receiveEntity(operationId, json));
        }).catch(error => {
          dispatch(this.flashMessagesManager.addErrorMessage({ title: this.i18n(`acc:content.provisioningOperations.action.${bulkActionName}.error`, { name: this.getNiceLabel(this.getEntity(getState(), operationId)) }) }, error));
          throw error;
        });
      }, Promise.resolve())
      .catch(() => {
        // nothing - message is propagated before
        // catch is before then - we want execute nex then clausule
      })
      .then(() => {
        if (successNames.length > 0) {
          dispatch(this.flashMessagesManager.addMessage({
            level: successNames.length === ids.length ? 'success' : 'info',
            message: this.i18n(`acc:content.provisioningOperations.action.${bulkActionName}.success`, { names: successNames.join(', ') })
          }));
          if (cb) {
            cb(successIds);
          }
        }
        dispatch(this.stopBulkAction());
      });
    };
  }
}
