import FormableEntityManager from './FormableEntityManager';
import { TreeNodeService } from '../../services';

/**
 * Tree nodes - items of tree scructures
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
export default class TreeNodeManager extends FormableEntityManager {

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
