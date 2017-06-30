import EntityManager from './EntityManager';
import { ScriptService } from '../../services';

export default class ScriptManager extends EntityManager {

  constructor() {
    super();
    this.service = new ScriptService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Script';
  }

  getCollectionType() {
    return 'scripts';
  }

  /**
   * Bulk operation for scripts action - backup, redeploy.
   *
   * @param  {[type]} entities
   * @param  {String} [operation='redeploy']
   * @param  {[type]} [uiKey=null]
   * @param  {[type]} [cb=null]
   * @return {[type]}
   *
   */
  scriptBulkOperationForEntities(entities, operation = 'redeploy', uiKey = null, cb = null) {
    return (dispatch) => {
      dispatch(
        this.startBulkAction(
          {
            name: operation,
            title: this.i18n(`action.${operation}.header`, { count: entities.length })
          },
          entities.length
        )
      );
      const successEntities = [];
      const failedEntities = [];
      entities.reduce((sequence, entity) => {
        return sequence.then(() => {
          return this.getService().scriptOperationById(entity.id, operation);
        }).then((json) => {
          if (json && json.statusCode === 404 && json.statusEnum === 'SCRIPT_XML_FILE_NOT_FOUND') {
            failedEntities.push(entity);
          } else {
            successEntities.push(entity);
            dispatch(this.receiveEntity(entity.id, json, uiKey));
          }
          dispatch(this.updateBulkAction());
        });
      }, Promise.resolve())
      .catch((error) => {
        if (error && error.statusEnum === 'BACKUP_FOLDER_NOT_FOUND') {
          dispatch(this.flashMessagesManager.addError(error));
        }
        return error;
      })
      .then((error) => {
        if (successEntities.length > 0) {
          dispatch(this.flashMessagesManager.addMessage({
            level: 'success',
            message: this.i18n(`action.${operation}.success`, { count: successEntities.length, records: this.getNiceLabels(successEntities).join(', '), record: this.getNiceLabel(successEntities[0]) })
          }));
        }
        if (failedEntities.length > 0) {
          dispatch(this.flashMessagesManager.addMessage({
            level: 'info',
            message: this.i18n(`action.${operation}.failed`, { count: failedEntities.length, records: this.getNiceLabels(failedEntities).join(', '), record: this.getNiceLabel(failedEntities[0]) })
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
