import EntityManager from './EntityManager';
import { TreeTypeService } from '../../services';
import DataManager from './DataManager';

/**
 * Tree structures
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
export default class TreeTypeManager extends EntityManager {

  constructor() {
    super();
    this.service = new TreeTypeService();
    this.dataManager = new DataManager();
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

  /**
   * Returns default tree type => organization structure
   *
   * @return {action}
   */
  fetchDefaultTreeType() {
    const uiKey = TreeTypeManager.UI_KEY_DEFAULT_TREE_TYPE;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getDefaultTreeType()
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

TreeTypeManager.UI_KEY_DEFAULT_TREE_TYPE = 'default-tree-type';
