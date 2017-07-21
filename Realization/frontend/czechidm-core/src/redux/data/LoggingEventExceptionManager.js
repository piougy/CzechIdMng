import EntityManager from './EntityManager';
import { LoggingEventExceptionService } from '../../services';

/**
 * Logging event exception manager
 *
 * @author Ondřej Kopr
 */
export default class LoggingEventExceptionManager extends EntityManager {

  constructor() {
    super();
    this.service = new LoggingEventExceptionService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'LoggingEventException';
  }

  getCollectionType() {
    return 'loggingEventExceptions';
  }
}
