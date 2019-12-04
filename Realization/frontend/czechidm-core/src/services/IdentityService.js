import * as Utils from '../utils';
import FormableEntityService from './FormableEntityService';
import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';
import PasswordPolicyService from './PasswordPolicyService';

const passwordPolicyService = new PasswordPolicyService();

/**
 * Identities endpoint
 *
 * @author Radek Tomiška
 */
class IdentityService extends FormableEntityService {

  getApiPath() {
    return '/identities';
  }

  supportsPatch() {
    return false;
  }

  supportsBulkAction() {
    return true;
  }

  getGroupPermission() {
    return 'IDENTITY';
  }

  supportsAttachment() {
    return true;
  }

  getNiceLabel(entity) {
    let toString = '';
    if (entity) {
      if (!entity.lastName) {
        return entity.username;
      }
      toString += this.getFullName(entity);
      toString += ` (${ entity.username }`;
      toString += (entity.externalCode ? `, ${entity.externalCode}` : '');
      toString += `)`;
    }
    return toString;
  }

  getFullName(entity) {
    let toString = '';
    if (entity) {
      toString += (entity.titleBefore ? (entity.titleBefore + ' ') : '');
      toString += (entity.firstName ? (entity.firstName + ' ') : '');
      toString += (entity.lastName ? entity.lastName : '');
      toString += (entity.titleAfter ? `, ${entity.titleAfter}` : '');
    }
    return toString;
  }

  /**
   * Change users pasword in given systems (by json)
   *
   * @param username {string}
   * @param passwordChangeDto {object}
   * @param token {string} - if token has to be used (from public page is is not needed, then *false* could be given)
   * @return {Promise}
   */
  passwordChange(username, passwordChangeDto, token = null) {
    return RestApiService.put(
      RestApiService.getUrl(`/public${ this.getApiPath() }/${ username }/password-change`),
      passwordChangeDto,
      token
    );
  }

  /**
   * Shows password policies rules on selected systems via exception
   *
   * @param passwordChangeDto {object}
   * @param token {string} - if token has to be used (from public page is is not needed, then *false* could be given)
   * @return {Promise}
   */
  preValidate(passwordChangeDto, token = null) {
    return RestApiService.put(RestApiService.getUrl(`/public${this.getApiPath()}/prevalidate`), passwordChangeDto, token);
  }

  /**
   * Generates password based on configured idm policies
   *
   * @return {Promise}
   */
  generatePassword() {
    return passwordPolicyService.generatePassword();
  }

  /**
   * Required idm password change for given user without login
   *
   * @param username {string}
   * @param oldPassword {string}
   * @param newPassword {string}
   * @return {Promise}
   */
  passwordMustChange(username, oldPassword, newPassword) {
    return RestApiService.put(`${ this.getApiPath() }/password/must-change`, {
      identity: username,
      oldPassword,
      newPassword
    });
  }

  /**
   * Incompatible roles are resolved from currently assigned identity roles
   *
   * @param username {string}
   * @param token {string}
   * @return {Promise}
   */
  getIncompatibleRoles(identityId, token = null) {
    return RestApiService
      .get(`${ this.getApiPath() }/${ identityId }/incompatible-roles`, token)
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

  sendLongPollingRequest(identityId) {
    return RestApiService
      .get(`${ this.getApiPath() }/${ identityId }/check-unresolved-request`)
      .then(response => response.json())
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('username');
  }

  /**
   * Deactivates given identity
   *
   * @param  {String} identity identifier
   * @return {Promise}
   */
  deactivate(id) {
    return RestApiService
      .patch(`${ this.getApiPath() }/${ id }/disable`, {})
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
   * Activates given identity
   *
   * @param  {String} identity identifier
   * @return {Promise}
   */
  activate(id) {
    return RestApiService
      .patch(`${ this.getApiPath() }/${ id }/enable`, {})
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
   * Returns identity accounts
   *
   * @param  {String} identity identifier
   * @return {Promise}
   */
  getAccounts(id) {
    return RestApiService.get(`${ this.getApiPath() }/${ id }/accounts`);
  }

  /**
   * Get given identity's main position in organization
   *
   * @param id {string} identity identifier
   * @param token {string}
   * @return {Promise}
   */
  getWorkPosition(id) {
    return RestApiService
      .get(`${ this.getApiPath() }/${ id }/work-position`)
      .then(response => {
        if (response.status === 204) {
          // no work position was found
          return {};
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

  getAuthorities(id) {
    return RestApiService
      .get(`${ this.getApiPath() }/${ id }/authorities`)
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

  /**
   * Start workflow for change permissions
   *
   * @param  id Identity id
   * @return Promise  task instance
   */
  changePermissions(id) {
    return RestApiService
      .put(`${ this.getApiPath() }/${ id }/change-permissions`, null)
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

  /**
   * Fetch profile
   *
   * @param  {string, number} identityId entity id
   * @param {string} token CIDMST token
   * @return {Promise}
   */
  getProfile(identityId, token = null) {
    return RestApiService
      .get(`${ this.getApiPath() }/${ identityId }/profile`, token)
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

  /**
   * Save (create + patch) profile
   *
   * @param  {string, number} identityId entity id
   * @param {Profile} profile
   * @return {Promise}
   */
  patchProfile(identityId, profile) {
    return RestApiService
      .patch(`${ this.getApiPath() }/${ identityId }/profile`, profile)
      .then(response => response.json())
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Upload image to BE
   */
  uploadProfileImage(identityId, formData) {
    return RestApiService
      .upload(`${ this.getApiPath() }/${ identityId }/profile/image`, formData)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        if (Utils.Response.hasInfo(json)) {
          throw Utils.Response.getFirstInfo(json);
        }
        return json;
      });
  }

  /**
   * Get image from BE
   */
  downloadProfileImage(identityId) {
    return RestApiService.download(`${ this.getApiPath() }/${ identityId }/profile/image`);
  }

  /**
   * Delete image from BE
   */
  deleteProfileImage(identityId) {
    return RestApiService
      .delete(`${ this.getApiPath() }/${ identityId }/profile/image`)
      .then(response => {
        if (response.status === 204) { // no content - ok
          return null;
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        if (Utils.Response.hasInfo(json)) {
          throw Utils.Response.getFirstInfo(json);
        }
        return json;
      });
  }

  /**
   * Fetch profile permissions
   *
   * @param  {string, number} identityId entity id
   * @return {Promise}
   */
  getProfilePermissions(identityId) {
    return RestApiService
      .get(`${ this.getApiPath() }/${ identityId }/profile/permissions`)
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

  /**
   * Get password by given identity identifier
   *
   * @param identityId {string} - identity identifier
   * @return {Promise}
   * @since 9.6.0
   */
  getPassword(identityId) {
    return RestApiService
      .get(`${ this.getApiPath() }/${ identityId }/password`)
      .then(response => {
        if (response.status === 204) { // no content - ok
          return false;
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

export default IdentityService;
