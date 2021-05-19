import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

/**
 * Scripts.
 *
 * @author Ondřej Kopr
 */
class ScriptService extends AbstractService {

  getApiPath() {
    return '/scripts';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    if (entity.name === entity.code) {
      return entity.name;
    }
    return `${ entity.name } (${ entity.code })`;
  }

  // dto
  supportsPatch() {
    return false;
  }

  supportsBulkAction() {
    return true;
  }

  /**
   * Returns default searchParameters for scripts
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }

  /**
   * Request operation defined by parameter, with id and GET method.
   *
   * @param  {[type]} id
   * @param  {[type]} operation
   * @return {[type]}
   */
  scriptOperationById(id, operation) {
    return RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/${ operation }`)
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
      }).catch((error) => {
        if (error && error.statusEnum === 'SCRIPT_XML_FILE_NOT_FOUND') {
          return error;
        }
        throw error;
      });
  }

  // script references
  _getScriptReferences(scriptId) {
    return RestApiService.get(`${ this.getApiPath() }/${ encodeURIComponent(scriptId) }/getScriptReferences`);
  }

  /**
   * Upload attachment.
   *
   * @param  {form} formData body
   * @return {object} attachment metadata
   * @since 10.6.0
   */
  deploy(formData) {
    return RestApiService
      .upload(`${ this.getApiPath() }/deploy`, formData)
      .then(response => {
        if (response.status === 204) {
          return {};
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
}

export default ScriptService;
