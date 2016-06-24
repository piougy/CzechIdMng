'use strict';

import AbstractEnum from '../../../modules/core/enums/AbstractEnum';

export default class CertificateTaskTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`crt:enums.CertificateTaskTypeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }
}

CertificateTaskTypeEnum.CREATE_CRT = Symbol('CREATE_CRT');
CertificateTaskTypeEnum.REVOCATION = Symbol('REVOCATION');
