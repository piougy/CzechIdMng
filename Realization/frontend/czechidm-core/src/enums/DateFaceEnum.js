import AbstractEnum from './AbstractEnum';

/**
 * Enumeration for dates filter
 *
 * @author Radek Tomiška
 */
export default class DateFaceEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.DateFaceEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }
}

DateFaceEnum.TODAY = Symbol('today');
DateFaceEnum.YESTERDAY = Symbol('yesterday');
DateFaceEnum.THIS_WEEK = Symbol('thisWeek');
DateFaceEnum.LAST_WEEK = Symbol('lastWeek');
DateFaceEnum.LAST_TWO_WEEKS = Symbol('lastTwoWeeks');
DateFaceEnum.LAST_SEVEN_DAYS = Symbol('lastSevenDays');
DateFaceEnum.THIS_MONTH = Symbol('thisMonth');
DateFaceEnum.LAST_MONTH = Symbol('lastMonth');
DateFaceEnum.LAST_THIRTY_DAYS = Symbol('lastThirtyDays');
DateFaceEnum.THIS_YEAR = Symbol('thisYear');
DateFaceEnum.BETWEEN = Symbol('between');
