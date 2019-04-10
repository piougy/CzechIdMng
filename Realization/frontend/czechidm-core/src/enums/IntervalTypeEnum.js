import AbstractEnum from './AbstractEnum';

/**
 * Interval type - minute, hour, day, week, month
 *
 * @author Petr Hanák
 */
export default class IntervalTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.IntervalTypeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

//   
  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.MINUTE: {
        return 'info';
        }
      case this.HOUR: {
        return 'success';
      }
      case this.DAY: {
        return 'info';
      }
      case this.WEEK: {
        return 'info';
      }
      case this.MONTH: {
        return 'warning';
      }
      default: {
        // nothing
      }
    }
  }
}

IntervalTypeEnum.MINUTE = Symbol('MINUTE');
IntervalTypeEnum.HOUR = Symbol('HOUR');
IntervalTypeEnum.DAY = Symbol('DAY');
IntervalTypeEnum.WEEK = Symbol('WEEK');
IntervalTypeEnum.MONTH = Symbol('MONTH');
