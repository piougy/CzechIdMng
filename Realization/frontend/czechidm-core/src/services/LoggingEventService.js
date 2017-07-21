import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Service for logging event exception
 *
 * @author Ondřej Kopr
 */

class LoggignEventService extends AbstractService {

  constructor() {
    super();
  }

  getApiPath() {
    return '/logging-events';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    // TODO: add better nice label
    return `${entity.id}`;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('id', 'DESC');
  }
}

export default LoggignEventService;
