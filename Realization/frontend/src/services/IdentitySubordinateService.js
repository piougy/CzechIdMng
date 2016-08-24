

import { RestApiService, IdentityService } from '../modules/core/services/';

class IdentitySubordinateService extends IdentityService {

  constructor(idmManager) {
    super();
    if (!idmManager) {
      throw new TypeError('idmManager is not defined');
    }
    this.idmManager = idmManager;
  }

  searchSubordinates(searchParameters) {
    super.searchSubordinates(this.idmManager, searchParameters);
  }

  search(searchParameters) {
    if (!searchParameters) {
      searchParameters = {};
    }
    return RestApiService.post(this.getApiPath() + `/${this.idmManager}/subordinates`, searchParameters);
  }

}

export default IdentitySubordinateService;
