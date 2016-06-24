'use strict';

import AbstractService from './AbstractService';

class IdentityRoleService extends AbstractService {

  getApiPath(){
    return '/identityRoles';
  }
}

export default IdentityRoleService;
