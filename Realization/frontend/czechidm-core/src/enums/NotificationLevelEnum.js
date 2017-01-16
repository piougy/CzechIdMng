import AbstractEnum from '../enums/AbstractEnum';

/**
 * OperationType for adit operation etc.
 */
export default class NotificationLevelEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.NotificationLevelEnum.${key}`);
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
      case this.INFO: {
        return 'info';
      }
      case this.SUCCESS: {
        return 'success';
      }
      case this.WARNING: {
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

NotificationLevelEnum.SUCCESS = Symbol('SUCCESS');
NotificationLevelEnum.INFO = Symbol('INFO');
NotificationLevelEnum.WARNING = Symbol('WARNING');
NotificationLevelEnum.ERROR = Symbol('ERROR');
