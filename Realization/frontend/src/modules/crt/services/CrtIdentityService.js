'use strict';

import { AbstractService } from '../../../modules/core/services/';

class CrtIdentityService extends AbstractService {

  getApiPath(){
    return '/idm/seam/resource/api-v1/cert-identities';
  }

  getNiceLabel(entity) {
    if (entity){
      return entity.identity;
    }
    return '-';
  }
}

export default CrtIdentityService;
