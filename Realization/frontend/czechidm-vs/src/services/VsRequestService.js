import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';

/**
 * Service controlls request for virtual systems
 *
 * @author Vít Švanda
 */
export default class VsRequestService extends Services.AbstractService {

  getApiPath() {
    return '/vs/requests';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.uid} (${entity.operationType} - ${entity.state})`;
  }

  /**
   * Agenda supports authorization policies
   */
  supportsAuthorization() {
    return true;
  }

  /**
   * Group permission - all base permissions (`READ`, `WRITE`, ...) will be evaluated under this group
   */
  getGroupPermission() {
    return 'VSREQUEST';
  }

  /**
   * Almost all dtos doesn§t support rest `patch` method
   */
  supportsPatch() {
    return false;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('uid');
  }
}
