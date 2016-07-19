

import { AbstractService, RestApiService, AuthenticateService } from '../../../modules/core/services';

class CrtCertificateService extends AbstractService {

  getApiPath(){
    return '/idm/seam/resource/api-v1/certificates';
  }

  getNiceLabel(entity) {
    if (entity){
      return entity.type + ' '+ entity.state ;
    }
    return '-';
  }

  revocation(id, json) {
    return RestApiService.put(this.getApiPath() + `/${id}`+'/revocation', json);
  }

  /**
   * Returns download url form given certificate type (pem, pfx ...)
   */
  getDownloadUrl(id, type) {
    const token = encodeURIComponent(AuthenticateService.getTokenCSRF());
    return RestApiService.getUrl(this.getApiPath() + `/${id}/download?type=${type}&xsrf=${token}`);
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    let defaultSearchParameters = super.getDefaultSearchParameters();
    defaultSearchParameters.sort = [
      {
        field: 'state',
        order: 'DESC'
      }
    ];
    return defaultSearchParameters;
  }
}

export default CrtCertificateService;
