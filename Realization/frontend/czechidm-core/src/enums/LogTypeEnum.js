import AbstractEnum from '../enums/AbstractEnum';

/**
 * Log type enum
 */
export default class LogTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.LogTypeEnum.${key}`);
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
    //
    const sym = super.findSymbolByKey(this, key);
    //
    switch (sym) {
      case this.INFO: {
        return 'info';
      }
      case this.DEBUG: {
        return 'success';
      }
      case this.TRACE: {
        return 'primary';
      }
      case this.WARN: {
        return 'warning';
      }
      case this.ERROR: {
        return 'danger';
      }
      default: {
        return 'default';
      }
    }
  }
}

LogTypeEnum.INFO = Symbol('INFO');
LogTypeEnum.DEBUG = Symbol('DEBUG');
LogTypeEnum.TRACE = Symbol('TRACE');
LogTypeEnum.WARN = Symbol('WARN');
LogTypeEnum.ERROR = Symbol('ERROR');
