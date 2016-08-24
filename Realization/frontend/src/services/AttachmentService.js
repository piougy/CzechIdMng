

import { AbstractService, RestApiService, AuthenticateService } from '../modules/core/services/';

class AttachmentService extends AbstractService {

  getApiPath() {
    return '/idm/seam/resource/api-v1/attachments';
  }

  getNiceLabel(entity) {
    return entity.name;
  }

  getDownloadUrl(id) {
    const token = encodeURIComponent(AuthenticateService.getTokenCSRF());
    return RestApiService.getUrl(this.getApiPath() + `/${id}/download?xsrf=${token}`);
  }

  upload(formData) {
    const token = AuthenticateService.getTokenCSRF();
    // doc: https://github.com/github/fetch
    return fetch(RestApiService.getUrl(this.getApiPath() + `/upload`), {
      method: 'post',
      headers: {
        'X-XSRF-TOKEN': token
      },
      credentials: 'include',
      body: formData
    });
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    const defaultSearchParameters = super.getDefaultSearchParameters();
    defaultSearchParameters.sort = [
      {
        field: 'name',
        order: 'ASC'
      }
    ];
    return defaultSearchParameters;
  }
}

export default AttachmentService;
