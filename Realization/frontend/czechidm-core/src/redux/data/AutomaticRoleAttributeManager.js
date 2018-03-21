import EntityManager from './EntityManager';
import { AutomaticRoleAttributeService } from '../../services';

/**
 * Automatic role manager by attribute
 *
 * @author Ondrej Kopr
 */
export default class AutomaticRoleAttributeManager extends EntityManager {

  constructor() {
    super();
    this.service = new AutomaticRoleAttributeService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'AutomaticRoleAttribute';
  }

  getCollectionType() {
    return 'automaticRoleAttributes';
  }

  /**
   * Recalucalte given automatic role by attribute
   */
  recalculate(id, callback = null) {
    this.getService().recalculate(id, callback);
  }

  /**
   * Delete automatic role - bulk action
   *
   * @param  {array[object]} entities - Entities to delete
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @param  {func} cb - function will be called after entities are deleted or error occured
   * @return {object} - action
   */
   deleteAutomaticRolesViaRequest(entities, uiKey = null, cb = null) {
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
           return this.getService().deleteAutomaticRolesViaRequest(entity.id);
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
