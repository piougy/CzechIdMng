import { ImportLogService } from '../../services';
import DataManager from './DataManager';
import EntityManager from './EntityManager';

/**
 * ImportLog manager
 *
 * @author Vít Švanda
 */
export default class ImportLogManager extends EntityManager {

  constructor() {
    super();
    this.service = new ImportLogService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  /**
   * Controlled entity
   */
  getEntityType() {
    return 'ImportLog';
  }

  /**
   * Collection name in search / find response
   */
  getCollectionType() {
    return 'logs';
  }
}
