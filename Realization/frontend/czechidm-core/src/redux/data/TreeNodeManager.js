import EntityManager from './EntityManager';
import { TreeNodeService } from '../../services';

export default class TreeNodeManager extends EntityManager {

  constructor() {
    super();
    this.service = new TreeNodeService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'TreeNode'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'treeNodes';
  }

  /**
   * Return search parameters for roots endpoint.
   * If you not specified treeType filter you get all roots, else you get one root.
   */
  getRootSearchParameters() {
    this.getService().getRootSearchParameters();
  }
}
