'use strict';

import IdentityManager from '../../modules/core/redux/data/IdentityManager';
import { IdentitySubordinateService } from '../../services';

/**
 * Manager for identity subordinates fetching
 */
export default class IdentitySubordinateManager extends IdentityManager {

  constructor(idmManager) {
    super();
    if (!idmManager) {
      throw new TypeError('idmManager is not defined');
    }
    this.identitySubordinateService = new IdentitySubordinateService(idmManager);
   }

  getService() {
    return this.identitySubordinateService;
  }
}
