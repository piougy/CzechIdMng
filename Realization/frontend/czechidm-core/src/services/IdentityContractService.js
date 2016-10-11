import moment from 'moment';
//
import AbstractService from './AbstractService';
import TreeNodeService from './TreeNodeService';

class IdentityWorkingPositionService extends AbstractService {

  constructor() {
    super();
    this.treeNodeService = new TreeNodeService();
  }

  getApiPath() {
    return '/identityContracts';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity._embedded && entity._embedded.workingPosition ? this.treeNodeService.getNiceLabel(entity._embedded.workingPosition) : entity.position;
  }

  isValid(identityContract) {
    // TODO: use entityUtils
    if (!identityContract || moment().isBefore(identityContract.validFrom) || moment().isAfter(identityContract.validTill)) {
      return false;
    }
    return true;
  }
}

export default IdentityWorkingPositionService;
