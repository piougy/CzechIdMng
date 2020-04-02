import AbstractEnum from './AbstractEnum';

/**
 * Interval type - minute, hour, day, week, month
 *
 * @author Petr Hanák
 * @since 10.2.0
 */
export default class IntervalTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.IntervalTypeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }
}

IntervalTypeEnum.MINUTE = Symbol('MINUTE');
IntervalTypeEnum.HOUR = Symbol('HOUR');
IntervalTypeEnum.DAY = Symbol('DAY');
IntervalTypeEnum.WEEK = Symbol('WEEK');
IntervalTypeEnum.MONTH = Symbol('MONTH');
