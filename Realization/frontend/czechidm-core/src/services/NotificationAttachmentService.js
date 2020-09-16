import RestApiService from './RestApiService';
import AbstractService from './AbstractService';
import AuthenticateService from './AuthenticateService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Notifacaton attachments.
 *
 * @author Radek Tomiška
 * @since 10.6.0
 */
export default class NotificationAttachmentService extends AbstractService {

  getApiPath() {
    return '/notification-attachments';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  supportsPatch() {
    return false;
  }

  /**
   * Returns default searchParameters for notification recipients
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }

  /**
   * Download notification attachment.
   *
   * @param  {string} notificationAttachmentId notification attachment identifier
   * @return {string} url
   */
  getDownloadUrl(notificationAttachmentId) {
    const _id = encodeURIComponent(notificationAttachmentId);
    const _token = AuthenticateService.getTokenCIDMST();
    //
    return RestApiService.getUrl(`${ this.getApiPath() }/${ _id }/download?cidmst=${ _token }`);
  }
}
