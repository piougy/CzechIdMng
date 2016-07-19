

import AbstractService from '../modules/core/services/AbstractService';

class AuditLogForObjectService extends AbstractService {

  getApiPath() {
    return '/idm/seam/resource/api-v1/audit/object';
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

export default AuditLogForObjectService;
