import EntityManager from './EntityManager';
import { LoggingEventService } from '../../services';

/**
 * Logging event manager
 *
 * @author Ondřej Kopr
 */
export default class LoggingEventManager extends EntityManager {

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
