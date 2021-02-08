import AbstractEnum from './AbstractEnum';

/**
 * Token types - provided by product.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
export default class TokenTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.TokenTypeEnum.${key}`);
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
      case this.CIDMST: {
        return 'success';
      }
      case this.SYSTEM: {
        return 'info';
      }
      default: {
        return 'default';
      }
    }
  }

  static getIcon(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.CIDMST: {
        return 'component:identity';
      }
      case this.SYSTEM: {
        return 'component:setting';
      }
      default: {
        return null;
      }
    }
  }
}

TokenTypeEnum.CIDMST = Symbol('CIDMST');
TokenTypeEnum.SYSTEM = Symbol('SYSTEM');
