import AbstractRequestService from './AbstractRequestService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import AuthenticateService from './AuthenticateService';
import * as Utils from '../utils';

/**
 * AttachmentService
 *
 * @author Radek Tomiška
 */

const DEFAULT_DOWNLOAD_LINK_PREFIX = '/attachments/';
const DEFAULT_DOWNLOAD_LINK_SUFFIX = '/download/';

export default class AttachmentService extends AbstractRequestService {

  getSubApiPath() {
    return '/attachments';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded) {
      return '';
    }
    return `${entity.name}`;
  }

  supportsPatch() {
    return true;
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
   * Upload attachment
   *
   * @param  {form} formData body
   * @return {object} attachment metadata
   */
  upload(formData) {
    return RestApiService
      .upload(`${ this.getApiPath() }/upload`, formData)
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
   * Get url for download attachment. Parameter downloadUrlPrefix is used for compose
   * download url itself.
   *
   * @param  {UUID} attachmentId
   * @param  {String} downloadUrlPrefix
   * @param  {String} downloadUrlSuffix
   * @return {String}
   */
  getDownloadUrl(attachmentId, downloadUrlPrefix, downloadUrlSuffix) {
    let downloadUrl = this.getApiPath();
    if (downloadUrlPrefix) {
      downloadUrl = `/${ downloadUrlPrefix }/${ encodeURIComponent(attachmentId) }`;
      if (downloadUrlSuffix) {
        downloadUrl = `${ downloadUrl }/${ downloadUrlSuffix }`;
      }
    } else {
      downloadUrl = `/${ DEFAULT_DOWNLOAD_LINK_PREFIX }/${ encodeURIComponent(attachmentId) }/${ DEFAULT_DOWNLOAD_LINK_SUFFIX }`;
    }
    downloadUrl = `${ downloadUrl }?cidmst=${ AuthenticateService.getTokenCIDMST() }`;
    return RestApiService.getUrl(downloadUrl);
  }
}
