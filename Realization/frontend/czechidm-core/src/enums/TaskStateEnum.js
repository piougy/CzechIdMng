

import AbstractEnum from '../enums/AbstractEnum';

/**
 * Approval task status enum
 */
export default class TaskStateEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.TaskStateEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.APPROVED: {
        return 'success';
      }
      case this.DENIED: {
        return 'danger';
      }
      case this.PENDING:
      case this.ESCALATED: {
        return 'warning';
      }
      default: {
        return 'default';
      }
    }
  }
}

TaskStateEnum.APPROVED = Symbol('APPROVED');
TaskStateEnum.DENIED = Symbol('DENIED');
TaskStateEnum.PENDING = Symbol('PENDING');
TaskStateEnum.ESCALATED = Symbol('ESCALATED');
