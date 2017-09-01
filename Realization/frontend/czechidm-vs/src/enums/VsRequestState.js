
import { Enums } from 'czechidm-core';

/**
 * State of request on virtual system
 *
 * @author Vít Švanda
 *
 */
export default class VsRequestState extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`vs:enums.VsRequestState.${key}`);
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
      case this.CONCEPT: {
        return 'default';
      }
      case this.EXECUTED: {
        return 'success';
      }
      case this.CANCELED: {
        return 'warning';
      }
      case this.REALIZED: {
        return 'success';
      }
      case this.REJECTED: {
        return 'danger';
      }
      case this.IN_PROGRESS: {
        return 'primary';
      }
      case this.EXCEPTION: {
        return 'danger';
      }
      case this.DUPLICATED: {
        return 'warning';
      }
      default: {
        return 'default';
      }
    }
  }
}

VsRequestState.CONCEPT = Symbol('CONCEPT');
VsRequestState.EXECUTED = Symbol('EXECUTED');
VsRequestState.CANCELED = Symbol('CANCELED');
VsRequestState.REALIZED = Symbol('REALIZED');
VsRequestState.REJECTED = Symbol('REJECTED');
VsRequestState.EXCEPTION = Symbol('EXCEPTION');
VsRequestState.IN_PROGRESS = Symbol('IN_PROGRESS');
VsRequestState.DUPLICATED = Symbol('DUPLICATED');
