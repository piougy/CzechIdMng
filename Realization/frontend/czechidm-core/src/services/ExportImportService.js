import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import AuthenticateService from './AuthenticateService';
import Response from '../utils/ResponseUtils';

/**
 * ExportImports
 *
 * @author Vít Švanda
 */
export default class ExportImportService extends AbstractService {

  getApiPath() {
    return '/export-imports';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.name}`;
  }

  /**
   * Agenda supports authorization policies
   */
  supportsAuthorization() {
    return true;
  }

  /**
   * Group permission - all base permissions (`READ`, `WRITE`, ...) will be evaluated under this group
   */
  getGroupPermission() {
    return 'EXPORTIMPORT';
  }

  /**
   * Almost all dtos doesn§t support rest `patch` method
   */
  supportsPatch() {
    return false;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }

  /**
   * Download export-imports
   *
   * @param  {string} id export-import identifier
   * @return {string} url
   */
  getDownloadUrl(id) {
    const _id = encodeURIComponent(id);
    const _token = AuthenticateService.getTokenCIDMST();
    //
    return RestApiService.getUrl(`${ this.getApiPath() }/${ _id }/download?&cidmst=${ _token }`);
  }

  execute(id, dryRun) {
    return RestApiService
      .put(`${ this.getApiPath() }/${ encodeURIComponent(id) }/execute-import?&dryRun=${ dryRun }`)
      .then(response => {
        return response.json();
      })
      .then(jsonResponse => {
        if (Response.hasError(jsonResponse)) {
          throw Response.getFirstError(jsonResponse);
        }
        if (Response.hasInfo(jsonResponse)) {
          throw Response.getFirstInfo(jsonResponse);
        }
        return jsonResponse;
      });
  }
}
