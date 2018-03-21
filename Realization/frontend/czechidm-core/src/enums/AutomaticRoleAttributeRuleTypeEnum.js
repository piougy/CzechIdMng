
import AbstractEnum from '../enums/AbstractEnum';

/**
 * Rules type
 */
export default class AutomaticRoleAttributeRuleTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.AutomaticRoleAttributeRuleTypeEnum.${key}`);
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
      case this.IDENTITY: {
        return 'success';
      }
      case this.IDENTITY_EAV: {
        return 'info';
      }
      case this.CONTRACT: {
        return 'danger';
      }
      case this.CONTRACT_EAV: {
        return 'warning';
      }
      default: {
        return 'default';
      }
    }
  }
}

AutomaticRoleAttributeRuleTypeEnum.IDENTITY = Symbol('IDENTITY');
AutomaticRoleAttributeRuleTypeEnum.IDENTITY_EAV = Symbol('IDENTITY_EAV');
AutomaticRoleAttributeRuleTypeEnum.CONTRACT = Symbol('CONTRACT');
AutomaticRoleAttributeRuleTypeEnum.CONTRACT_EAV = Symbol('CONTRACT_EAV');
