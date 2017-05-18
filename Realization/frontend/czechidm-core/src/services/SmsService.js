import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Sms logs
 *
 * @author Peter Sourek
 */
export default class SmsService extends AbstractService {

  getApiPath() {
    return '/notification-sms';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.message.subject;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }
}
