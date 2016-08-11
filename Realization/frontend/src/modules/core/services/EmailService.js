import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

export default class EmailService extends AbstractService {

  getApiPath() {
    return '/emails';
  }

  getNiceLabel(email) {
    if (!email) {
      return '';
    }
    return email.message.subject;
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
