'use strict';

import AbstractEnum from '../../../modules/core/enums/AbstractEnum';

export default class CertificateStateEnum extends AbstractEnum {

  static getNiceLabel(key){
    return super.getNiceLabel(`crt:enums.CertificateStateEnum.${key}`);
  }

  static findKeyBySymbol(sym){
    return super.findKeyBySymbol(this, sym);
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);
    switch (sym) {
      case this.VALID: {
        return 'success'; // TODO: primary or not?
      }
      case this.REVOKED: {
        return 'danger';
      }
      case this.EXPIRED: {
        return 'default';
      }
      default: {
        return 'default';
      }
    }
  }
}

CertificateStateEnum.VALID  = Symbol('VALID');
CertificateStateEnum.REVOKED  = Symbol('REVOKED');
CertificateStateEnum.EXPIRED  = Symbol('EXPIRED');
