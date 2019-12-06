import { Services, Domain, Utils } from 'czechidm-core';

/**
 * Reports
 *
 * @author Radek Tomiška
 */
export default class ReportService extends Services.AbstractService {

  getApiPath() {
    return '/rpt/reports';
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
    return 'REPORT';
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
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }

  /**
   * Download report with selected renderer ~ format
   *
   * @param  {string} reportId report identifier
   * @param  {string} rendererName renderer name ~ renderer identifier
   * @return {string} url
   */
  getDownloadUrl(reportId, rendererName) {
    const _id = encodeURIComponent(reportId);
    const _name = encodeURIComponent(rendererName);
    const _token = Services.AuthenticateService.getTokenCIDMST();
    //
    return Services.RestApiService.getUrl(`${ this.getApiPath() }/${ _id }/render?renderer=${ _name }&cidmst=${ _token }`);
  }

  /**
   * Loads all registered reports
   *
   * @return {promise}
   */
  getSupportedReports() {
    return Services.RestApiService
      .get(`${ this.getApiPath() }/search/supported`)
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
