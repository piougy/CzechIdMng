import AbstractEnum from './AbstractEnum';

/**
 * Day in week - monday, tuesday, wednesday..
 *
 * @author Petr Hanák
 */
export default class WeekDayEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.WeekDayEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }
}

WeekDayEnum.MONDAY = Symbol('mon');
WeekDayEnum.TUESDAY = Symbol('tue');
WeekDayEnum.WEDNESDAY = Symbol('wed');
WeekDayEnum.THURSTDAY = Symbol('thu');
WeekDayEnum.FRIDAY = Symbol('fri');
WeekDayEnum.SATURDAY = Symbol('sat');
WeekDayEnum.SUNDAY = Symbol('sun');
