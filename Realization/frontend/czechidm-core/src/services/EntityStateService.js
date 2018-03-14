import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Entity states
 *
 * @author Radek Tomiška
 */
export default class EntityStateService extends AbstractService {

  getApiPath() {
    return '/entity-states';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    // TODO: which attribute can be used?
    return entity.id;
  }

  /**
   * Returns default searchParameters for scripts
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }
}
