import AbstractService from './AbstractService';
import RequestService from './RequestService';

class UniversalRequestService extends AbstractService {

  constructor(requestId, originalService) {
    super();
    this.originalService = originalService;
    this.requestId = requestId;
    this.requestService = new RequestService();
  }

  getApiPath() {
    return `${this.requestService.getApiPath()}/${this.requestId}${this.originalService.getApiPath()}`;
  }

  getNiceLabel(entity) {
    return this.originalService.getNiceLabel(entity);
  }

  supportsAuthorization() {
    return this.originalService.supportsAuthorization();
  }

  getGroupPermission() {
    return this.originalService.getGroupPermission();
  }

  /**
   * Patch is not supported in requests
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
    return this.originalService.getDefaultSearchParameters();
  }

}

export default UniversalRequestService;
