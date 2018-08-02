import EntityManager from './EntityManager';

export default class AbstractRequestManager extends EntityManager {

  constructor() {
    super();
  }

  setRequestId(requestId) {
    this.getService().setRequestId(requestId);
  }

  getEntityType() {
    if (this.isRequestModeEnabled()) {
      return `Request-${this.getEntitySubType()}`;
    }
    return this.getEntitySubType();
  }

  isRequestModeEnabled() {
    return this.getService().isRequestModeEnabled();
  }
}
