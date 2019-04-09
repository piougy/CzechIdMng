import * as Utils from '../utils';
import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';

export default class PasswordService extends AbstractService {


  getApiPath() {
    return '/passwords';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  // dto
  supportsPatch() {
    return false;
  }

  supportsAuthorization() {
    return true;
  }

  /**
   * Returns default searchParameters for passwords
   *
   * @return {SearchParameters} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created');
  }

  /**
   * Get password by given identity identifier
   *
   * @param identityId {string} - identity identifier
   * @return {Promise}
   */
  getPassword(identityIdentifier) {
    return RestApiService
    .get(this.getApiPath() + `/search/identity/${encodeURIComponent(identityIdentifier)}`)
    .then(response => {
      return response.json();
    })
    .then(json => {
      if (Utils.Response.hasError(json)) {
        throw Utils.Response.getFirstError(json);
      }
      return json;
    });
  }
}
