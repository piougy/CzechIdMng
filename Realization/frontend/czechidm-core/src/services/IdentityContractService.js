import moment from 'moment';
//
import AbstractService from './AbstractService';
import TreeNodeService from './TreeNodeService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Identity contracts - relation to tree structure
 *
 * @author Radek Tomiška
 */
class IdentityContractService extends AbstractService {

  constructor() {
    super();
    this.treeNodeService = new TreeNodeService();
  }

  getApiPath() {
    return '/identity-contracts';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity._embedded && entity._embedded.workingPosition ? this.treeNodeService.getNiceLabel(entity._embedded.workingPosition) : entity.position;
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
