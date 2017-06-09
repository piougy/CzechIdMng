import AbstractEnum from '../enums/AbstractEnum';

/**
 * Notification status enum
 */
export default class NotificationStateEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.NotificationStateEnum.${key}`);
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
      case this.ALL: {
        return 'success';
      }
      case this.PARTLY: {
        return 'warning';
      }
      case this.NOT: {
        return 'danger';
      }
      default: {
        return 'default';
      }
    }
  }
}

NotificationStateEnum.ALL = Symbol('ALL');
NotificationStateEnum.PARTLY = Symbol('PARTLY');
NotificationStateEnum.NOT = Symbol('NOT');
