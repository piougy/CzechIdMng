import FormableEntityManager from './FormableEntityManager';
import { LoggingEventService } from '../../services';

/**
 * Logging event manager
 *
 * @author Ondřej Kopr
 */
export default class LoggingEventManager extends FormableEntityManager {

  constructor() {
    super();
    this.service = new LoggingEventService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'LoggingEvent';
  }

  getCollectionType() {
    return 'loggingEvents';
  }
}
