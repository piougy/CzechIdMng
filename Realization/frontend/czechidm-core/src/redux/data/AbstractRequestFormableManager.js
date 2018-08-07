import FormableEntityManager from './FormableEntityManager';

export default class AbstractRequestFormableManager extends FormableEntityManager {

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

  /**
   * Authorization evaluator helper - evaluates save permission on given entity.
   * If isn't request mode enabled, then entity cannot be saved.
   *
   * If entity is null - CREATE is evaluated
   *
   * @param  {object} entity
   * @param  {arrayOf(string)} permissions
   * @return {bool}
   */
  canSave(entity = null, permissions = null) {
    if (!this.isRequestModeEnabled()) {
      return false;
    }
    return super.canSave(entity, permissions);
  }

  /**
   * Authorization evaluator helper - evaluates delete permission on given entity.
   * If isn't request mode enabled, then entity cannot be saved.
   *
   * @param  {object} entity
   * @param  {arrayOf(string)} permissions
   * @return {bool}
   */
  canDelete(entity = null, permissions = null) {
    if (!this.isRequestModeEnabled()) {
      return false;
    }
    return super.canDelete(entity, permissions);
  }

  /**
   * Authorization evaluator helper - evaluates execute permission on given entity.
   * If isn't request mode enabled, then entity cannot be saved.
   *
   * @param  {object} entity
   * @param  {arrayOf(string)} permissions
   * @return {bool}
   */
  canExecute(entity = null, permissions = null) {
    if (!this.isRequestModeEnabled()) {
      return false;
    }
    return super.canExecute(entity, permissions);
  }
}
