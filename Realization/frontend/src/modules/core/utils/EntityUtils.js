'use strict';

import _ from 'lodash';
import Immutable from 'immutable';
import moment from 'moment';

/**
 * Helper methods for entities
 */
export default class EntityUtils {

  /**
   * Returns entity, if entity is contained in applicateion state.
   * Can be used in select state.
   *
   * @param  {state} state - application state
   * @param  {string} entityType - entity type (e.g. Identity)
   * @param  {string|number} id - entity identifier
   * @return {object} - entity
   */
  static getEntity(state, entityType, id) {
    if (!state || !entityType || !id) {
      return null;
    }
    if (!state.data.entity || !state.data.entity[entityType]) {
      return null;
    }
    const stateEntities = state.data.entity[entityType];
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
   * @return {array[object]}
   */
  static getEntitiesByIds(state, entityType, ids = []) {
    if (!state || !entityType || !ids || ids.length === 0) {
      return [];
    }
    return ids
      .map(id => {
        return EntityUtils.getEntity(state, entityType, id);
      })
      .filter(entity => {
        return entity !== null
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
    if (moment().isBefore(entity.validFrom) || moment().isAfter(entity.validTill)) {
      return false;
    }
    return true;
  }
}
