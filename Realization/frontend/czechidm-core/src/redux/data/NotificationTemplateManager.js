import EntityManager from './EntityManager';
import { NotificationTemplateService } from '../../services';

export default class NotificationTemplateManager extends EntityManager {

  constructor() {
    super();
    this.service = new NotificationTemplateService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'NotificationTemplate';
  }

  getCollectionType() {
    return 'notificationTemplates';
  }

  notificationBulkOperationForEntities(entities, operation = 'redeploy', uiKey = null, cb = null) {
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
          return this.getService().notificationOperationById(entity.id, operation);
        }).then((json) => {
          if (json && json.statusCode === 404 && json.statusEnum === 'NOTIFICATION_TEMPLATE_XML_FILE_NOT_FOUND') {
            failedEntities.push(entity);
          } else if (json && json.statusCode === 404 && json.statusEnum === 'NOTIFICATION_TEMPLATE_BACKUP_FAIL') {
            failedEntities.push(entity);
          } else {
            successEntities.push(entity);
            dispatch(this.receiveEntity(entity.id, json, uiKey));
          }
          dispatch(this.updateBulkAction());
        });
      }, Promise.resolve())
      .catch((error) => {
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
            level: 'warning',
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
