import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';

class SystemService extends Services.AbstractService {

  getApiPath() {
    return '/systems';
  }

  getNiceLabel(system) {
    if (!system) {
      return '';
    }
    return system.name;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }
}

export default SystemService;
