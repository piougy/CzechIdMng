import { Enums } from 'czechidm-core';

/**
 * OperationType for adit operation etc.
 */
export default class ProvisioningResultStateEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.ProvisioningResultStateEnum.${key}`);
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
      case this.CREATED: {
        return 'info';
      }
      case this.EXECUTED: {
        return 'success';
      }
      case this.NOT_EXECUTED: {
        return 'warning';
      }
      case this.EXCEPTION: {
        return 'danger';
      }
      case this.CANCELED: {
        return 'default';
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
      case this.CREATED: {
        return 'fa:circle-o';
      }
      case this.EXECUTED: {
        return 'fa:check';
      }
      case this.NOT_EXECUTED: {
        return 'fa:circle-thin';
      }
      case this.EXCEPTION: {
        return 'fa:warning';
      }
      case this.CANCELED: {
        return 'fa:ban';
      }
      default: {
        return 'default';
      }
    }
  }
}

ProvisioningResultStateEnum.CREATED = Symbol('CREATED');
ProvisioningResultStateEnum.EXECUTED = Symbol('EXECUTED');
ProvisioningResultStateEnum.EXCEPTION = Symbol('EXCEPTION');
ProvisioningResultStateEnum.NOT_EXECUTED = Symbol('NOT_EXECUTED');
ProvisioningResultStateEnum.CANCELED = Symbol('CANCELED');
