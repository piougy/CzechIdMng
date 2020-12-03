import * as Utils from '../utils';
import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Profiles
 *
 * @author Radek Tomiška
 */
class ProfileService extends AbstractService {

  getApiPath() {
    return '/profiles';
  }

  supportsPatch() {
    return true;
  }

  supportsBulkAction() {
    return true;
  }

  getGroupPermission() {
    return 'PROFILE';
  }

  /**
   * Nice label
   *
   * @param  {entity} entity
   * @return {string}
   */
  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.id; // TODO ... Profile - identity nice label
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('identity.username', 'asc');
  }

  /**
   * Init two factor authentication method.
   *
   * @param  {String} profile identifier
   * @param  {String} two factor authentication method
   * @return {Promise}
   * @since 10.6.0
   */
  twoFactorAuthenticationInit(id, twoFactorAuthenticationType) {
    return RestApiService
      .put(`${ this.getApiPath() }/${ encodeURIComponent(id) }/two-factor/init?twoFactorAuthenticationType=${ twoFactorAuthenticationType }`, {})
      .then(response => {
        return response.json();
      })
      .then(jsonResponse => {
        if (Utils.Response.hasError(jsonResponse)) {
          throw Utils.Response.getFirstError(jsonResponse);
        }
        return jsonResponse;
      });
  }

  /**
   * Confirm two factor authentication method.
   *
   * @param  {String} profile identifier
   * @param  {String} two factor authentication method
   * @param  {TwoFactorRegistrationConfirmDto} two factor tomken and verificatio code
   * @return {Promise}
   * @since 10.6.0
   */
  twoFactorAuthenticationConfirm(id, twoFactorConfirm) {
    return RestApiService
      .put(
        `${ this.getApiPath() }/${ encodeURIComponent(id) }/two-factor/confirm`,
        twoFactorConfirm
      )
      .then(response => {
        return response.json(); // updated profile
      })
      .then(jsonResponse => {
        if (Utils.Response.hasError(jsonResponse)) {
          throw Utils.Response.getFirstError(jsonResponse);
        }
        return jsonResponse;
      });
  }
}

export default ProfileService;
