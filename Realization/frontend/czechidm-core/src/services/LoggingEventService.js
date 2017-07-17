import FormableEntityService from './FormableEntityService';
import SearchParameters from '../domain/SearchParameters';

class LoggignEventService extends FormableEntityService {

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
    return `${entity.eventId}`;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('eventId', 'DESC');
  }
}

export default LoggignEventService;
