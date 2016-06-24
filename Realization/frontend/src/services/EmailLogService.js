'use strict';

import AbstractService from '../modules/core/services/AbstractService';

class EmailLogService extends AbstractService {

  getApiPath() {
    return '/idm/seam/resource/api-v1/audit/email';
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
        field: 'createdAt',
        order: 'DESC'
      }
    ];
    return defaultSearchParameters;
  }
}

export default EmailLogService;
