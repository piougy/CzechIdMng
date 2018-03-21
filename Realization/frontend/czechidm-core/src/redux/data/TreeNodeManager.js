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

  /**
   * Returns default tree node => organization structure
   *
   * @return {action}
   */
  fetchDefaultTreeNode() {
    const uiKey = TreeNodeManager.UI_KEY_DEFAULT_TREE_NODE;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getDefaultTreeNode()
        .then(json => {
          dispatch(this.receiveEntity(json.id, json, uiKey));
          dispatch(this.dataManager.receiveData(uiKey, json));
        })
        .catch(error => {
          if (error.statusCode === 404) {
            dispatch(this.dataManager.receiveData(uiKey, null));
          } else {
            // TODO: data uiKey
            dispatch(this.dataManager.receiveError(null, uiKey, error));
          }
        });
    };
  }
}

TreeNodeManager.UI_KEY_DEFAULT_TREE_NODE = 'default-tree-node';
