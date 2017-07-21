import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Service for loggging event exception
 *
 * @author Ondřej Kopr
 */

class LoggignEventExceptionService extends AbstractService {

  constructor() {
    super();
  }

  getApiPath() {
    return '/logging-event-exceptions';
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
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('id', 'ASC').setSize(100);
  }
}

export default LoggignEventExceptionService;
