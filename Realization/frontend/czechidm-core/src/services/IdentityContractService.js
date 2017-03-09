import moment from 'moment';
//
import FormableEntityService from './FormableEntityService';
import TreeNodeService from './TreeNodeService';
import IdentityService from './IdentityService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Identity contracts - relation to tree structure
 *
 * @author Radek Tomiška
 */
class IdentityContractService extends FormableEntityService {

  constructor() {
    super();
    this.treeNodeService = new TreeNodeService();
    this.identityService = new IdentityService();
  }

  getApiPath() {
    return '/identity-contracts';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    if (!entity._embedded) {
      return entity.position;
    }
    let niceLabel = null;
    if (entity._embedded.identity) {
      niceLabel = this.identityService.getNiceLabel(entity._embedded.identity);
    }
    let positionName = entity.position;
    if (entity._embedded.workingPosition) {
      positionName = this.treeNodeService.getNiceLabel(entity._embedded.workingPosition);
    }
    return niceLabel ? `${niceLabel} - ${positionName}` : positionName;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('validFrom', 'asc');
  }

  isValid(identityContract) {
    // TODO: use entityUtils
    if (!identityContract || moment().isBefore(identityContract.validFrom) || moment().isAfter(identityContract.validTill)) {
      return false;
    }
    return true;
  }
}

export default IdentityContractService;
