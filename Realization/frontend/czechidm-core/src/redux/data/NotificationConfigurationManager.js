import EntityManager from './EntityManager';
import { NotificationConfigurationService } from '../../services';
import DataManager from './DataManager';

export default class NotificationConfigurationManager extends EntityManager {

  constructor() {
    super();
    this.service = new NotificationConfigurationService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'NotificationConfiguration';
  }

  getCollectionType() {
    return 'notificationConfigurations';
  }

  fetchSupportedNotificationTypes() {
    const uiKey = NotificationConfigurationManager.SUPPORTED_NOTIFICATION_TYPES;
    //
    return (dispatch, getState) => {
      const supportedNotificationTypes = DataManager.getData(getState(), uiKey);
      if (supportedNotificationTypes) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getSupportedNotificationTypes()
          .then(json => {
            dispatch(this.dataManager.receiveData(uiKey, json));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.receiveError(null, uiKey, error));
          });
      }
    };
  }
}

NotificationConfigurationManager.SUPPORTED_NOTIFICATION_TYPES = 'supported-notification-types';
