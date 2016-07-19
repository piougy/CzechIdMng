

import { AbstractService, RestApiService } from '../modules/core/services/';

class IdentityDelegateService extends AbstractService {

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
    // TODO: tohle asi neme valneho smyslu - najit vypovidajici reprezentaci delegace
    return entity.delegate + ' (' + entity.identity + ')';
  }

  search(searchParameters) {
    if (!this.username) {
      return null;
    }
    return RestApiService.get(this.getApiPath() + `/${this.username}/delegates`);
  }

  createDelegate(delegateDto) {
    return RestApiService.post(this.getApiPath() + `/${delegateDto.identity}/delegates`, delegateDto);
  }

  deleteDelegate(delegateDto) {
    return RestApiService.deleteById(this.getApiPath() + `/${delegateDto.identity}/delegates/${delegateDto.id}`);
  }
}

export default IdentityDelegateService;
