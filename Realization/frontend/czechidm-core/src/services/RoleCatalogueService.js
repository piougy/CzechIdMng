import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class RoleCatalogueService extends AbstractService {

  getApiPath() {
    return '/roleCatalogues';
  }

  getNiceLabel(roleCatalogue) {
    if (!roleCatalogue) {
      return '';
    }
    return `${roleCatalogue.name}`;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }

  /**
   * Returns search parameters for search roots
   */
  getRootSearchParameters() {
    return super.getDefaultSearchParameters().setName(RoleCatalogueService.ROOT_SEARCH).clearSort().setSort('name');
  }

  /**
   * Search children by parent id
   */
  getTreeSearchParameters() {
    return super.getDefaultSearchParameters().setName(RoleCatalogueService.TREE_SEARCH).clearSort().setSort('name');
  }
}

/**
 * Search children by parent id
 */
RoleCatalogueService.TREE_SEARCH = 'children';

/**
 * Search roots
 */
RoleCatalogueService.ROOT_SEARCH = 'roots';

export default RoleCatalogueService;
