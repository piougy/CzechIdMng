import { Enums } from 'czechidm-core';

/**
 * Type of strategy for attribut mapping (provisioning and synchronization)
 */
export default class AttributeMappingStrategyTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.AttributeMappingStrategyTypeEnum.${key}`);
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
      case this.MERGE: {
        return 'success';
      }
      case this.AUTHORITATIVE_MERGE: {
        return 'warning';
      }
      case this.SET: {
        return 'success';
      }
      case this.CREATE: {
        return 'success';
      }
      case this.WRITE_IF_NULL: {
        return 'success';
      }
      default: {
        return 'default';
      }
    }
  }
}

AttributeMappingStrategyTypeEnum.MERGE = Symbol('MERGE');
AttributeMappingStrategyTypeEnum.AUTHORITATIVE_MERGE = Symbol('AUTHORITATIVE_MERGE');
AttributeMappingStrategyTypeEnum.SET = Symbol('SET');
AttributeMappingStrategyTypeEnum.CREATE = Symbol('CREATE');
AttributeMappingStrategyTypeEnum.WRITE_IF_NULL = Symbol('WRITE_IF_NULL');
