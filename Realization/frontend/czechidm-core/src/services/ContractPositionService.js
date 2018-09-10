import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import IdentityContractService from './IdentityContractService';
import TreeNodeService from './TreeNodeService';

/**
 * Identity's contract other positions
 *
 * @author Radek Tomiška
 * @since 9.1.0
 */
export default class ContractPositionService extends AbstractService {

  constructor() {
    super();
    //
    this.identityContractService = new IdentityContractService();
    this.treeNodeService = new TreeNodeService();
  }

  getApiPath() {
    return '/contract-positions';
  }

  getNiceLabel(entity, showIdentity = true) {
    if (!entity || !entity._embedded) {
      return '';
    }
    if (!entity._embedded) {
      return `${entity.id}`;
    }
    let positionLabel = null;
    if (entity._embedded.workPosition) {
      positionLabel = this.treeNodeService.getNiceLabel(entity._embedded.workPosition);
    }
    const position = entity.position ? entity.position : 'default';
    if (positionLabel === null) {
      positionLabel = position; // TODO: locale or make at least one of position / tree node required!
    } else {
      positionLabel = `${positionLabel}, ${position}`;
    }
    if (!showIdentity) {
      return positionLabel;
    }
    return `${ this.identityContractService.getNiceLabel(entity._embedded.identityContract) } - ${ positionLabel }`;
  }

  supportsPatch() {
    return true;
  }

  getGroupPermission() {
    return 'CONTRACTPOSITION';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('position', 'asc');
  }
}
