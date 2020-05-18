import AbstractEnum from './AbstractEnum';

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
      case this.NOT_EQUALS: {
        return 'warning';
      }
      case this.START_WITH: {
        return 'success';
      }
      case this.NOT_START_WITH: {
        return 'warning';
      }
      case this.END_WITH: {
        return 'success';
      }
      case this.NOT_END_WITH: {
        return 'warning';
      }
      case this.IS_EMPTY: {
        return 'success';
      }
      case this.IS_NOT_EMPTY: {
        return 'warning';
      }
      case this.CONTAINS: {
        return 'success';
      }
      case this.NOT_CONTAINS: {
        return 'warning';
      }
      case this.LESS_THAN_OR_EQUAL: {
        return 'info';
      }
      case this.GREATER_THAN_OR_EQUAL: {
        return 'info';
      }
      default: {
        return 'default';
      }
    }
  }
}

AutomaticRoleAttributeRuleComparisonEnum.EQUALS = Symbol('EQUALS');
AutomaticRoleAttributeRuleComparisonEnum.NOT_EQUALS = Symbol('NOT_EQUALS');
AutomaticRoleAttributeRuleComparisonEnum.START_WITH = Symbol('START_WITH');
AutomaticRoleAttributeRuleComparisonEnum.NOT_START_WITH = Symbol('NOT_START_WITH');
AutomaticRoleAttributeRuleComparisonEnum.END_WITH = Symbol('END_WITH');
AutomaticRoleAttributeRuleComparisonEnum.NOT_END_WITH = Symbol('NOT_END_WITH');
AutomaticRoleAttributeRuleComparisonEnum.IS_EMPTY = Symbol('IS_EMPTY');
AutomaticRoleAttributeRuleComparisonEnum.IS_NOT_EMPTY = Symbol('IS_NOT_EMPTY');
AutomaticRoleAttributeRuleComparisonEnum.CONTAINS = Symbol('CONTAINS');
AutomaticRoleAttributeRuleComparisonEnum.NOT_CONTAINS = Symbol('NOT_CONTAINS');
AutomaticRoleAttributeRuleComparisonEnum.LESS_THAN_OR_EQUAL = Symbol('LESS_THAN_OR_EQUAL');
AutomaticRoleAttributeRuleComparisonEnum.GREATER_THAN_OR_EQUAL = Symbol('GREATER_THAN_OR_EQUAL');
