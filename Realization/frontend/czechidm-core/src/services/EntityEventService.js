import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import LocalizationService from './LocalizationService';
import moment from 'moment';

/**
 * Entity events and states
 *
 * @author Radek Tomiška
 */
export default class EntityEventService extends AbstractService {

  getApiPath() {
    return '/entity-events';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${moment(entity.created).format(LocalizationService.i18n('format.date'))}: ${entity.eventType}`;
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
