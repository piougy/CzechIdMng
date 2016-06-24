'use strict';

import EntityManager from '../../../../modules/core/redux/data/EntityManager';
import { CrtIdentityService } from '../../services';

const service = new CrtIdentityService();

/**
 * Manager for certificate identity fetching
 */
export default class CrtIdentityManager extends EntityManager {

  getService() {
    return service;
  }

  getEntityType() {
    return 'cert-identity'; // TODO: constant or enumeration
  }
}
