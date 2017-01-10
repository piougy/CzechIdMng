import AbstractEnum from '../enums/AbstractEnum';

/**
 * Rule category enums.
 * Mor information see BE class IdmRuleCategory.java
 * with all definition.
 */
export default class RuleCategoryEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.RuleCategoryEnum.${key}`);
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
    // TODO? Add some label like in role type?
    return 'default';
  }
}

RuleCategoryEnum.DEFAULT = Symbol('DEFAULT');
RuleCategoryEnum.TRANSFORM_FROM = Symbol('TRANSFORM_FROM');
RuleCategoryEnum.TRANSFORM_TO = Symbol('TRANSFORM_TO');
RuleCategoryEnum.SYSTEM = Symbol('SYSTEM');
