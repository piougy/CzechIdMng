import AbstractEnum from '../enums/AbstractEnum';

/**
 * Universal operation state
 *
 * @author Radek Tomiška
 */
export default class OperationStateEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`enums.OperationStateEnum.${key}`);
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
      case this.CREATED:
      case this.RUNNING: {
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
      case this.BLOCKED: {
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
      case this.CREATED:
      case this.RUNNING: {
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
      case this.BLOCKED: {
        return 'fa:stop-circle';
      }
      default: {
        return 'default';
      }
    }
  }
}

OperationStateEnum.CREATED = Symbol('CREATED');
OperationStateEnum.RUNNING = Symbol('RUNNING');
OperationStateEnum.EXECUTED = Symbol('EXECUTED');
OperationStateEnum.EXCEPTION = Symbol('EXCEPTION');
OperationStateEnum.NOT_EXECUTED = Symbol('NOT_EXECUTED');
OperationStateEnum.CANCELED = Symbol('CANCELED');
OperationStateEnum.CANCELED = Symbol('BLOCKED');
