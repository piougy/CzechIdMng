
import AbstractEnum from '../enums/AbstractEnum';

/**
 * The role priority / criticality enum
 */
export default class RolePriorityEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.RolePriorityEnum.${key}`);
  }

  static getNiceLabelBySymbol(sym) {
    return this.getNiceLabel(this.findKeyBySymbol(sym));
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
      case this.NONE: {
        return 'default';
      }
      case this.TRIVIAL: {
        return 'success';
      }
      case this.MINOR: {
        return 'success';
      }
      case this.MAJOR: {
        return 'warning';
      }
      case this.CRITICAL: {
        return 'danger';
      }
      default: {
        return 'default';
      }
    }
  }

  static getPriority(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.NONE: {
        return 0;
      }
      case this.TRIVIAL: {
        return 1;
      }
      case this.MINOR: {
        return 2;
      }
      case this.MAJOR: {
        return 3;
      }
      case this.CRITICAL: {
        return 4;
      }
      default: {
        return 0;
      }
    }
  }

  static getKeyByPriority(priority) {
    if (!priority && priority !== 0) {
      return null;
    }

    switch (priority) {
      case 0: {
        return this.NONE;
      }
      case 1: {
        return this.TRIVIAL;
      }
      case 2: {
        return this.MINOR;
      }
      case 3: {
        return this.MAJOR;
      }
      case 4: {
        return this.CRITICAL;
      }
      default: {
        return this.NONE;
      }
    }
  }
}

RolePriorityEnum.NONE = Symbol('NONE');
RolePriorityEnum.TRIVIAL = Symbol('TRIVIAL');
RolePriorityEnum.MINOR = Symbol('MINOR');
RolePriorityEnum.MAJOR = Symbol('MAJOR');
RolePriorityEnum.CRITICAL = Symbol('CRITICAL');
