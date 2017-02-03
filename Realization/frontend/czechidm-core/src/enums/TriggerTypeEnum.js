import AbstractEnum from './AbstractEnum';

/**
 * Trigger type - simple - cron
 */
export default class TriggerTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.TriggerTypeEnum.${key}`);
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
      case this.SIMPLE: {
        return 'success';
      }
      case this.CRON: {
        return 'info';
      }
      default: {
        // nothing
      }
    }
  }
}

TriggerTypeEnum.SIMPLE = Symbol('SIMPLE');
TriggerTypeEnum.CRON = Symbol('CRON');
