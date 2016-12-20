
import AbstractEnum from '../enums/AbstractEnum';

/**
 * Audit modification enum
 */
export default class AuditModificationEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.AuditModificationEnum.${key}`);
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
      case this.ADD: {
        return 'success';
      }
      case this.MOD: {
        return 'warning';
      }
      case this.DEL: {
        return 'danger';
      }
      default: {
        return 'default';
      }
    }
  }
}

AuditModificationEnum.ADD = Symbol('ADD');
AuditModificationEnum.DEL = Symbol('DEL');
AuditModificationEnum.MOD = Symbol('MOD');
