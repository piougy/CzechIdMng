import EntityManager from './EntityManager';
import { AutomaticRoleAttributeRuleService } from '../../services';

/**
 * Rules for automatic role manager by attribute
 *
 * @author Ondrej Kopr
 */
export default class AutomaticRoleAttributeRuleManager extends EntityManager {

  constructor() {
    super();
    this.service = new AutomaticRoleAttributeRuleService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'AutomaticRoleAttributeRule';
  }

  getCollectionType() {
    return 'automaticRoleAttributeRules';
  }

  /**
   * Create new entity and recalculate all automatic roles by attribute
   *
   * @param  {object} entity - Entity to patch
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @param  {func} cb - function will be called after entity is patched or error occured
   * @return {object} - action
   */
  createAndRecalculateEntity(entity, uiKey = null, cb = null) {
    if (!entity) {
      return {
        type: EntityManager.EMPTY
      };
    }
    uiKey = this.resolveUiKey(uiKey, '[new]');
    return (dispatch) => {
      dispatch(this.requestEntity('[new]', uiKey));
      this.getService().createAndRecalculate(entity)
      .then(json => {
        dispatch(this.receiveEntity(json.id, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(entity, uiKey, error, cb));
      });
    };
  }

  /**
   * Update entity and recalculate all automatic roles by attribute
   *
   * @param  {object} entity - Entity to update
   * @param  {string} uiKey = null - ui key for loading indicator etc
   * @param  {func} cb - function will be called after entity is updated or error occured
   * @return {object} - action
   */
  updateAndRecalculateEntity(entity, uiKey = null, cb = null) {
    if (!entity) {
      return {
        type: EntityManager.EMPTY
      };
    }
    uiKey = this.resolveUiKey(uiKey, entity.id);
    return (dispatch) => {
      dispatch(this.requestEntity(entity.id, uiKey));
      this.getService().updateByIdAndRecalculate(entity.id, entity)
      .then(json => {
        dispatch(this.receiveEntity(entity.id, json, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(entity, uiKey, error, cb));
      });
    };
  }
}
