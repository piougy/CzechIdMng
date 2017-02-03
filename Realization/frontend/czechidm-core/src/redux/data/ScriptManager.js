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
}
