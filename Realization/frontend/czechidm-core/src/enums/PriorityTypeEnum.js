import AbstractEnum from '../enums/AbstractEnum';

/**
 * Task / event priority
 *
 * @author Radek Tomiška
 */
export default class PriorityTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`enums.PriorityTypeEnum.${key}`);
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
      case this.HIGH: {
        return 'primary';
      }
      case this.IMMEDIATE: {
        return 'danger';
      }
      default: {
        return 'success';
      }
    }
  }

  static getIcon(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.IMMEDIATE: {
        return 'fa:warning';
      }
      case this.HIGH: {
        return 'fa:angle-up';
      }
      default: {
        return 'fa:circle-o';
      }
    }
  }
}

PriorityTypeEnum.IMMEDIATE = Symbol('IMMEDIATE');
PriorityTypeEnum.HIGH = Symbol('HIGH');
PriorityTypeEnum.NORMAL = Symbol('NORMAL');
