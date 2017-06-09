import { Enums } from 'czechidm-core';

/**
 * IcFilterOperationTypeEnum for filter on target system.
 */
export default class IcFilterOperationTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.IcFilterOperationTypeEnum.${key}`);
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
      default: {
        return 'default';
      }
    }
  }
}

IcFilterOperationTypeEnum.EQUAL_TO = Symbol('EQUAL_TO');
IcFilterOperationTypeEnum.CONTAINS = Symbol('CONTAINS');
IcFilterOperationTypeEnum.LESS_THAN = Symbol('LESS_THAN');
IcFilterOperationTypeEnum.GREATER_THAN = Symbol('GREATER_THAN');
IcFilterOperationTypeEnum.ENDS_WITH = Symbol('ENDS_WITH');
IcFilterOperationTypeEnum.STARTS_WITH = Symbol('STARTS_WITH');
