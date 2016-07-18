

import { AbstractService, RestApiService } from '../../../modules/core/services/';

class CrtCertificateTaskService extends AbstractService {

  getApiPath(){
    return '/idm/seam/resource/api-v1/certificate-tasks';
  }

  getNiceLabel(entity) {
    if (entity){
      return entity.type + ' '+ entity.state ;
    }
    return '-';
  }

  refreshProcessState(id, json) {
    return RestApiService.put(this.getApiPath() + `/${id}`+'/refresh-process-state', json);
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
        field: 'submission',
        order: 'DESC'
      }
    ];
    return defaultSearchParameters;
  }
}

export default CrtCertificateTaskService;
