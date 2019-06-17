import FormableEntityManager from './FormableEntityManager';
import { RequestIdentityRoleService } from '../../services';

export const EMPTY = 'VOID_ACTION'; // dispatch cannot return null

export default class RequestIdentityRoleManager extends FormableEntityManager {
  constructor() {
    super();
    this.service = new RequestIdentityRoleService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'RequestIdentityRole';
  }

  getCollectionType() {
    return 'requestIdentityRoles';
  }

  /**
   * Delete entity
   *
   * @param  {object} entity - Entity to delete
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @param  {func} cb - function will be called after entity is deleted or error occured
   * @return {object} - action
   */
  deleteEntity(entity, uiKey = null, cb = null) {
    if (!entity) {
      return {
        type: EMPTY
      };
    }
    uiKey = this.resolveUiKey(uiKey, entity.id);
    return (dispatch) => {
      dispatch(this.requestEntity(entity.id, uiKey));
      this.getService().delete(entity)
      .then((json) => {
        if (cb) {
          cb(json);
        }
        // dispatch(this.deletedEntity(entity.id, entity, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(entity, uiKey, error, cb));
      });
    };
  }

  /**
   * Delete entities - bulk action
   *
   * @param  {array[object]} entities - Entities to delete
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @param  {func} cb - function will be called after entities are deleted or error occured
   * @return {object} - action
   */
   deleteEntities(entities, uiKey = null, cb = null) {
     return (dispatch) => {
       dispatch(
         this.startBulkAction(
           {
             name: 'delete',
             title: this.i18n(`action.delete.header`, { count: entities.length })
           },
           entities.length
         )
       );
       const successEntities = [];
       const approveEntities = [];
       let currentEntity = null; // currentEntity in loop
       entities.reduce((sequence, entity) => {
         return sequence.then(() => {
           // stops when first error occurs
           currentEntity = entity;
           return this.getService().delete(entity);
         }).then(() => {
           dispatch(this.updateBulkAction());
           successEntities.push(entity);
           // remove entity to redux store
           dispatch(this.deletedEntity(entity.id, entity, uiKey));
         }).catch(error => {
           if (error && error.statusCode === 202) {
             dispatch(this.updateBulkAction());
             approveEntities.push(entity);
           } else {
             if (currentEntity.id === entity.id) { // we want show message for entity, when loop stops
               if (!cb) { // if no callback given, we need show error
                 dispatch(this.flashMessagesManager.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: this.getNiceLabel(entity) }) }, error));
               } else { // otherwise caller has to show eror etc. himself
                 cb(entity, error, null);
               }
             }
             throw error;
           }
         });
       }, Promise.resolve())
       .catch((error) => {
         // nothing - message is propagated before
         // catch is before then - we want execute next then clausule
         return error;
       })
       .then((error) => {
         if (successEntities.length > 0) {
           dispatch(this.flashMessagesManager.addMessage({
             level: 'success',
             message: this.i18n(`action.delete.success`, { count: successEntities.length, records: this.getNiceLabels(successEntities).join(', '), record: this.getNiceLabel(successEntities[0]) })
           }));
         }
         if (approveEntities.length > 0) {
           dispatch(this.flashMessagesManager.addMessage({
             level: 'info',
             message: this.i18n(`action.delete.accepted`, { count: approveEntities.length, records: this.getNiceLabels(approveEntities).join(', '), record: this.getNiceLabel(approveEntities[0]) })
           }));
         }
         dispatch(this.stopBulkAction());
         if (cb) {
           cb(null, error, successEntities);
         }
       });
     };
   }
}
