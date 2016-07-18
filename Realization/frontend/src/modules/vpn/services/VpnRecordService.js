

import { AbstractService, RestApiService } from '../../../modules/core/services/';

class VpnRecordService extends AbstractService {

  getApiPath(){
    return '/idm/seam/resource/api-v1/vpn-records';
  }

  getNiceLabel(entity) {
    if (entity){
      return entity.owner ;
    }
    return '-';
  }

  invalidate(id) {
    return RestApiService.put(this.getApiPath() + `/${id}`+'/invalidate');
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
        field: 'currentActivity.state',
        order: 'ASC'
      }
    ];
    return defaultSearchParameters;
  }
}

export default VpnRecordService;
