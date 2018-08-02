import FormableEntityManager from './FormableEntityManager';

export default class AbstractRequestFormableManager extends FormableEntityManager {

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
