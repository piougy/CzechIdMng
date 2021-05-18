import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Role catalogue.
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
class RoleCatalogueService extends AbstractService {

  getApiPath() {
    return '/role-catalogues';
  }

  /**
   * Extended nice label.
   *
   * @param  {entity} entity
   * @param  {boolean} showCode code be rendered.
   * @return {string} nicelabel
   * @since 11.1.0
   */
  getNiceLabel(entity, showCode = true) {
    if (!entity) {
      return '';
    }
    if (entity.name === entity.code || !showCode) {
      return entity.name;
    }
    return `${entity.name} (${entity.code})`;
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
    return super.getDefaultSearchParameters()
      .setName(RoleCatalogueService.ROOT_SEARCH)
      .clearSort()
      .setSort('name')
      .setSize(25);
  }

  /**
   * Search children by parent id
   */
  getTreeSearchParameters() {
    return super.getDefaultSearchParameters()
      .setName(RoleCatalogueService.TREE_SEARCH)
      .clearSort()
      .setSort('name')
      .setSize(25);
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
