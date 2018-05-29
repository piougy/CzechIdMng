import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

/**
 * TODO: Add better constant for max roots count
 * @type {Number}
 */
const MAX_ROOTS_COUNT = 100000;

/**
 * Role catalogue
 *
 * @author Ondřej Kopr
 */
class RoleCatalogueService extends AbstractService {

  getApiPath() {
    return '/role-catalogues';
  }

  getNiceLabel(roleCatalogue) {
    if (!roleCatalogue) {
      return '';
    }
    return `${roleCatalogue.name} (${roleCatalogue.code})`;
  }

  supportsPatch() {
    return false;
  }

  getGroupPermission() {
    return 'ROLECATALOGUE';
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
    return super.getDefaultSearchParameters().setName(RoleCatalogueService.ROOT_SEARCH).clearSort().setSort('name').setSize(MAX_ROOTS_COUNT);
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
