import EntityManager from './EntityManager';
import { TreeTypeService } from '../../services';

export default class TreeTypeManager extends EntityManager {

  constructor() {
    super();
    this.service = new TreeTypeService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'TreeType';
  }

  getCollectionType() {
    return 'treeTypes';
  }
}
