import AbstractService from './AbstractService';
import RequestService from './RequestService';

class AbstractRequestService extends AbstractService {

  constructor() {
    super();
    this.requestService = new RequestService();
  }

  setRequestId(requestId) {
    this.requestId = requestId;
  }

  getApiPath() {
    if (this.isRequestModeEnabled()) {
      return `${this.requestService.getApiPath()}/${this.requestId}${this.getSubApiPath()}`;
    }
    return this.getSubApiPath();
  }

  isRequestModeEnabled() {
    if (this.requestId) {
      return true;
    }
    return false;
  }

}

export default AbstractRequestService;
