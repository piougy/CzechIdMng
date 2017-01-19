import _ from 'lodash';
import moment from 'moment';
import Immutable from 'immutable';
//
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

  /**
   * Retry or cancel provisioning operation
   *
   * @param  {[type]} _ids           [description]
   * @param  {[type]} bulkActionName [description]
   * @param  {[type]} batch          =             true [description]
   * @param  {[type]} cb             =             null [description]
   * @return {[type]}                [description]
   */
  retry(_ids, bulkActionName, batch = true, cb = null) {
    return (dispatch, getState) => {
      // prepare ids
      let ids;
      if (batch) {
        // resolve batch ids;
        let idSet = new Immutable.Set();
        _ids.forEach(id => {
          idSet = idSet.add(this.getEntity(getState(), id).request.batch.id);
        });
        //
        ids = idSet.toArray();
      } else {
        // sort ids by created date
        ids = _.sortBy(_ids, (id) => {
          return moment(this.getEntity(getState(), id).created);
        });
      }
      //
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
      //
      ids.reduce((sequence, operationId) => {
        return sequence.then(() => {
          if (batch) {
            return this.getService().retryBatch(operationId, bulkActionName);
          }
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
            level: 'info',
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
