import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Delegation service.
 *
 * @author Vít Švanda
 * @since 10.4.0
 */
class DelegationService extends AbstractService {

  getApiPath() {
    return '/delegations';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    const ownerType = entity.ownerType;
    let label = entity.ownerId;
    if (ownerType) {
      label += ` - ${ ownerType }`;
    }
    //
    return label;
  }

  getGroupPermission() {
    return 'DELEGATIONDEFINITION';
  }

  supportsPatch() {
    return false;
  }

  supportsBulkAction() {
    return true;
  }

  /**
   * Returns default searchParameters for current entity type.
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }
}

export default DelegationService;
