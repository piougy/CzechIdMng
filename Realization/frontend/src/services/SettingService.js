'use strict';

import AbstractService from '../modules/core/services/AbstractService';

class SettingService extends AbstractService {

  getApiPath(){
    return '/idm/seam/resource/api-v1/settings';
  }

  getNiceLabel(entity){
    return entity.key + ': ' + entity.value;
  }
}

export default SettingService;
