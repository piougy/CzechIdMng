import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RoleService from './RoleService';
import TreeNodeService from './TreeNodeService';

/**
 * Automatic roles administration
 *
 * @author Radek Tomiška
 */
export default class RoleTreeNodeService extends AbstractService {

  constructor() {
    super();
    this.roleService = new RoleService();
    this.treeNodeService = new TreeNodeService();
  }

  getApiPath() {
    return '/role-tree-nodes';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded) {
      return '';
    }
    return `${this.roleService.getNiceLabel(entity._embedded.role)} - ${this.treeNodeService.getNiceLabel(entity._embedded.treeNode)}`;
  }

  supportsPatch() {
    return false;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('treeNode.name', 'asc');
  }
}
