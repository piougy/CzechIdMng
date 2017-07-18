import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

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
    return `${entity.i}`;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('id', 'ASC');
  }
}

export default LoggignEventExceptionService;
