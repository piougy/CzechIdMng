import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import IdentityContractService from './IdentityContractService';
import IdentityService from './IdentityService';

/**
 * Identity's contract guarantee - manually defined  managers (if no tree structure is defined etc.)
 *
 * @author Radek Tomiška
 */
export default class ContractGuaranteeService extends AbstractService {

  constructor() {
    super();
    //
    this.identityContractService = new IdentityContractService();
    this.identityService = new IdentityService();
  }

  getApiPath() {
    return '/contract-guarantees';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    if (!entity._embedded) {
      return `${ entity.id }`;
    }
    const contractNiceLabel = this.identityContractService.getNiceLabel(entity._embedded.identityContract);
    const guaranteeNiceLabel = this.identityService.getNiceLabel(entity._embedded.guarantee);
    //
    return `${ contractNiceLabel } - ${ guaranteeNiceLabel }`;
  }

  supportsPatch() {
    return false;
  }

  getGroupPermission() {
    return 'CONTRACTGUARANTEE';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('guarantee.username', 'asc');
  }
}
