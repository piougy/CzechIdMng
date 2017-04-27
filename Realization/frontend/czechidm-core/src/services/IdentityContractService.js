import FormableEntityService from './FormableEntityService';
import TreeNodeService from './TreeNodeService';
import IdentityService from './IdentityService';
import SearchParameters from '../domain/SearchParameters';
import * as Utils from '../utils';

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

  supportsPatch() {
    return false;
  }

  /**
   * Extended nice label
   *
   * @param  {entity} entity
   * @param  {boolean} showIdentity identity will be rendered.
   * @return {string}
   */
  getNiceLabel(entity, showIdentity = true) {
    if (!entity) {
      return '';
    }
    if (!entity._embedded) {
      return entity.position;
    }
    let niceLabel = null;
    if (showIdentity && entity._embedded.identity) {
      niceLabel = this.identityService.getNiceLabel(entity._embedded.identity);
    }
    let positionName = entity.position;
    if (entity._embedded.workPosition) {
      positionName = this.treeNodeService.getNiceLabel(entity._embedded.workPosition);
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
    return Utils.Entity.isValid(identityContract);
  }
}

export default IdentityContractService;
