

import { AbstractService } from '../../../modules/core/services/';

class VpnActivityService extends AbstractService {

  getApiPath(){
    return '/idm/seam/resource/api-v1/vpn-activities';
  }

  getNiceLabel(entity) {
    if (entity){
      return entity.state ;
    }
    return '-';
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

export default VpnActivityService;
