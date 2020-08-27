import { Services, Domain } from 'czechidm-core';

/**
 * Service for uniform password and connection to system
 *
 * @author Ondrej Kopr
 */
export default class UniformPasswordSystemService extends Services.AbstractService {

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    if (entity._embedded && entity._embedded.system) {
      return `${ entity._embedded.system.name }`;
    }
    return '';
  }

  getApiPath() {
    return '/uniform-password-systems';
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'UNIFORMPASSWORD';
  }

  // dto
  supportsPatch() {
    return true;
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('system.name');
  }
}
