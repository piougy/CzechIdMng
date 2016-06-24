'use strict';

import AbstractEnum from '../../../modules/core/enums/AbstractEnum';

/**
 * Users role assign type
 */
export default class RoleAssignTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.RoleAssignTypeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.ASSIGNED:
      case this.FINITE_ASSIGNMENT: {
        return 'success';
      }
      case this.QUARANTINE:
      case this.WAIT_CHANGE_APPROVE:
      case this.WAIT_REMOVE_APPROVE: {
        return 'warning';
      }
      default: {
        return 'default';
      }
    }
  }
}

RoleAssignTypeEnum.ASSIGNED = Symbol('ASSIGNED');
RoleAssignTypeEnum.WAIT_ASSIGN = Symbol('WAIT_ASSIGN');
RoleAssignTypeEnum.FINITE_ASSIGNMENT = Symbol('FINITE_ASSIGNMENT');
RoleAssignTypeEnum.QUARANTINE = Symbol('QUARANTINE');
RoleAssignTypeEnum.UNASSIGNED = Symbol('UNASSIGNED');
RoleAssignTypeEnum.WAIT_APPROVE = Symbol('WAIT_APPROVE');
RoleAssignTypeEnum.WAIT_CHANGE_APPROVE = Symbol('WAIT_CHANGE_APPROVE');
RoleAssignTypeEnum.WAIT_REMOVE_APPROVE = Symbol('WAIT_REMOVE_APPROVE');
