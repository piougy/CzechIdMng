

import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class OrganizationService extends AbstractService {

  const
  getApiPath() {
    return '/organizations';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }

  /**
   * Returns search parameters by parent ID, used in tree
   */
  getTreeSearchParameters() {
    return super.getDefaultSearchParameters().setName(OrganizationService.TREE_SEARCH).clearSort().setSort('name');
  }
}

/**
 * Search by parent ID for tree
 * @type {Number}
 */
OrganizationService.TREE_SEARCH = 'children';

export default OrganizationService;
