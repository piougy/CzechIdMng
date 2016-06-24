'use strict';

import AbstractEnum from '../../../modules/core/enums/AbstractEnum';

export default class VpnActivityStateEnum extends AbstractEnum {

  static getNiceLabel(key) {
    let result =  super.getNiceLabel(`vpn:enums.VpnActivityStateEnum.${key}`);
    if (`enums.VpnActivityStateEnum.${key}` === result){
      return key;
    }
    return result;
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
      case this.TO_IMPLEMENTATION:
      case this.TO_GARANT_APPROVE: {
        return 'warning';
      }
      case this.INVALIDATED:
      case this.DISAPPROVED:
      case this.DISAPPROVED_BY_REALIZATOR:
      case this.DISAPPROVED_BY_GARANT: {
        return 'danger';
      }
      case this.IMPLEMENTED:
      case this.NEW_REQUEST:
      case this.APPROVED_BY_GARANT: {
        return 'success';
      }
      default: {
        return 'default';
      }
    }
  }
}

VpnActivityStateEnum.TO_IMPLEMENTATION  = Symbol('TO_IMPLEMENTATION');
VpnActivityStateEnum.APPROVED_BY_GARANT  = Symbol('APPROVED_BY_GARANT');
VpnActivityStateEnum.IMPLEMENTED  = Symbol('IMPLEMENTED');
VpnActivityStateEnum.DISAPPROVED_BY_GARANT  = Symbol('DISAPPROVED_BY_GARANT');
VpnActivityStateEnum.DISAPPROVED_BY_REALIZATOR  = Symbol('DISAPPROVED_BY_REALIZATOR');
VpnActivityStateEnum.DISAPPROVED  = Symbol('DISAPPROVED');
VpnActivityStateEnum.TO_GARANT_APPROVE  = Symbol('TO_GARANT_APPROVE');
VpnActivityStateEnum.NEW_REQUEST  = Symbol('NEW_REQUEST');
