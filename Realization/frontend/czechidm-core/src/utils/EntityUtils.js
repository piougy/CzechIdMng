import moment from 'moment';

/**
 * Helper methods for entities
 *
 * @author Radek Tomiška
 */
export default class EntityUtils {

  /**
   * Returns entity, if entity is contained in application state.
   * Can be used in select state.
   *
   * @param  {state} state - application state
   * @param  {string} entityType - entity type (e.g. Identity)
   * @param  {string|number} id - entity identifier
   * @param {bool} trimmed - trimmed or full entity is needed
   * @return {object} - entity
   */
  static getEntity(state, entityType, id, trimmed = false) {
    if (trimmed !== null) {
      return this._getEntity(state, entityType, id, trimmed);
    }
    // not trimmed entity has higher priority
    const entity = this._getEntity(state, entityType, id, false);
    if (!entity) {
      return this._getEntity(state, entityType, id, true);
    }
    return entity;
  }

  /**
   * Returns entity, if entity is contained in application state.
   * Can be used in select state.
   *
   * @param  {state} state - application state
   * @param  {string} entityType - entity type (e.g. Identity)
   * @param  {string|number} id - entity identifier
   * @param {bool} trimmed - trimmed or full entity is needed
   * @return {object} - entity
   */
  static _getEntity(state, entityType, id, trimmed = false) {
    if (!state || !entityType || !id) {
      return null;
    }
    const store = trimmed ? state.data.trimmed : state.data.entity;
    if (!store || !store[entityType]) {
      return null;
    }
    const stateEntities = store[entityType];
    if (!stateEntities.has(id)) {
      return null;
    }
    return stateEntities.get(id);
  }

  /**
   *	Returns entities by ids, if entities are contained in applicateion state.
   *
   * @param  {state} state [description]
   * @param  {string} entityType - entity type (e.g. Identity)
   * @param  {array[string|number]} ids  entity ids
   * @param {bool} trimmed - trimmed or full entity is needed
   * @return {array[object]}
   */
  static getEntitiesByIds(state, entityType, ids = [], trimmed = null) {
    if (!state || !entityType || !ids || ids.length === 0) {
      return [];
    }
    return ids
      .map(id => {
        return this.getEntity(state, entityType, id, trimmed);
      })
      .filter(entity => {
        return entity !== null;
      });
  }

  /**
   * Returns true, when given entity has property `disabled` setted to `true`
   *
   * @param  {object} entity
   * @return {Boolean}
   */
  static isDisabled(entity) {
    if (!entity) {
      return false;
    }
    if (entity.disabled && entity.disabled === true) {
      return true;
    }
    return false;
  }

  /**
   * Returns true, when given entity is valid (not contains validTill and validFrom or current date is between ...)
   *
   * @param  {object} entity
   * @return {Boolean}
   */
  static isValid(entity) {
    if (!entity) {
      return false;
    }
    // entity does not support validable
    if (entity.validFrom === undefined && entity.validTill === undefined) {
      return true;
    }
    if ((entity.validFrom !== null && moment().isBefore(entity.validFrom)) || (entity.validTill !== null && moment().subtract(1, 'days').isAfter(entity.validTill))) {
      return false;
    }
    return true;
  }

  /**
   * Returns true, if entity does not contain id (respectively entity._links.self)
   *
   * @param  {object}  entity
   * @return {Boolean}
   */
  static isNew(entity) {
    // TODO: new dtos doesn't contain self link
    // return !entity || !entity._links || !entity._links.self;
    return !entity || !entity.id;
  }
}
