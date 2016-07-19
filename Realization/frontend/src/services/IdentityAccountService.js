

import { AbstractService, RestApiService } from '../modules/core/services/';

class IdentityAccountService extends AbstractService {

  constructor(username) {
    super();
    this.username = username;
   }

   setUsername(username) {
     this.username = username;
   }

  getApiPath() {
    return '/idm/seam/resource/api-v1/identities';
  }

  getNiceLabel(entity) {
    return entity.resource + ' (' +entity.identity + ')';
  }

  search(searchParameters) {
    if (!this.username) {
      return null;
    }
    if (!searchParameters) {
      searchParameters = {};
    }
    return RestApiService.get(this.getApiPath() + `/${this.username}/accounts`);
  }
}

export default IdentityAccountService;
