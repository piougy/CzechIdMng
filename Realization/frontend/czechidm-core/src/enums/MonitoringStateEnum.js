import AbstractEnum from './AbstractEnum';

/**
 * Basic state of a monitoring.
 *
 * @author Vít Švanda
 */
export default class MonitoringStateEnum extends AbstractEnum {

  static getNiceLabel() {
    // return super.getNiceLabel(`enums.MonitoringStateEnum.${key}`);
    return '';
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
      case this.OK: {
        return 'success';
      }
      case this.ERROR: {
        return 'danger';
      }
      case this.WARNING: {
        return 'warning';
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
      case this.OK: {
        return 'fa:check';
      }
      case this.WARNING: {
        return 'fa:warning';
      }
      case this.ERROR: {
        return 'fa:ban';
      }
      default: {
        return 'default';
      }
    }
  }
}

MonitoringStateEnum.OK = Symbol('OK');
MonitoringStateEnum.WARNING = Symbol('WARNING');
MonitoringStateEnum.ERROR = Symbol('ERROR');
