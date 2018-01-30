import AbstractEnum from '../enums/AbstractEnum';

/**
 * Comparsion types
 */
export default class AutomaticRoleAttributeRuleComparisonEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.AutomaticRoleAttributeRuleComparisonEnum.${key}`);
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
      case this.EQUALS: {
        return 'success';
      }
      default: {
        return 'default';
      }
    }
  }
}

AutomaticRoleAttributeRuleComparisonEnum.EQUALS = Symbol('EQUALS');
