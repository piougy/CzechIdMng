'use strict';

import AbstractService from '../modules/core/services/AbstractService';

class AuditLogService extends AbstractService {

  getApiPath() {
    return '/idm/seam/resource/api-v1/audit/log';
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
        field: 'date',
        order: 'DESC'
      }
    ];
    return defaultSearchParameters;
  }
}

export default AuditLogService;
