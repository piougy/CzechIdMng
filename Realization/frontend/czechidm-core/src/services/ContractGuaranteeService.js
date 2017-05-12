import AbstractService from './AbstractService';
// import SearchParameters from '../domain/SearchParameters';

/**
 * Identity's contract guarantee - manually defined  managers (if no tree structure is defined etc.)
 *
 * @author Radek Tomiška
 */
export default class ContractGuaranteeService extends AbstractService {

  constructor() {
    super();
  }

  getApiPath() {
    return '/contract-guarantees';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded) {
      return '';
    }
    return `${entity.id}`; // TODO: identityContract.niceLabel + guarantee.niceLabel
  }

  supportsPatch() {
    return false;
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'CONTRACTGUARANTEE';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  /* getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('guarantee.username', 'asc');
  }*/
}
