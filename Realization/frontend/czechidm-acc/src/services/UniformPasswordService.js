import { Services, Domain, Utils } from 'czechidm-core';

/**
 * Uniform password service
 *
 * @author Ondrej Kopr
 */
export default class UniformPasswordrService extends Services.AbstractService {

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.code}`;
  }

  getApiPath() {
    return '/uniform-passwords';
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'UNIFORMPASSWORD';
  }

  // dto
  supportsPatch() {
    return true;
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('code');
  }

  /**
   * Return available password change options for given identity. For read the options is requred read
   * @param  {string} identity id
   * @return {[type]}    [description]
   */
  getPasswordChangeOptions(id) {
    return Services.RestApiService
      .get(`${ this.getApiPath() }/search/password-change-options/${ encodeURIComponent(id) }`)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
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
