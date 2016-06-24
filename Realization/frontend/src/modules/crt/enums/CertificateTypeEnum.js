'use strict';

import AbstractEnum from '../../../modules/core/enums/AbstractEnum';

export default class CertificateTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`crt:enums.CertificateTypeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }
}

CertificateTypeEnum.AUTHETICATION = Symbol('AUTHETICATION');
CertificateTypeEnum.SIGNING = Symbol('SIGNING');
CertificateTypeEnum.ENCRYPTION = Symbol('ENCRYPTION');
CertificateTypeEnum.VPN = Symbol('VPN');
