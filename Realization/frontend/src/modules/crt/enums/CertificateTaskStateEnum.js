

import AbstractEnum from '../../../modules/core/enums/AbstractEnum';

export default class CertificateTaskStateEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`crt:enums.CertificateTaskStateEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);
    switch (sym) {
      case this.IN_PROGRESS: {
        return 'warning'; // TODO: primary or not? VS: vote warning
      }
      case this.ERROR: {
        return 'danger';
      }
      case this.REVOKED:
      case this.RELEASED: {
        return 'success';
      }
      case this.NOT_RELEASED: {
        return 'default';
      }
      default: {
        return 'default';
      }
    }
  }
}

CertificateTaskStateEnum.IN_PROGRESS  = Symbol('IN_PROGRESS');
CertificateTaskStateEnum.RELEASED  = Symbol('RELEASED');
CertificateTaskStateEnum.NOT_RELEASED  = Symbol('NOT_RELEASED');
CertificateTaskStateEnum.ERROR  = Symbol('ERROR');
CertificateTaskStateEnum.REVOKED  = Symbol('REVOKED');
