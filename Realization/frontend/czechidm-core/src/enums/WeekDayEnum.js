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

WeekDayEnum.MONDAY = Symbol('MONDAY');
WeekDayEnum.TUESDAY = Symbol('TUESDAY');
WeekDayEnum.WEDNESDAY = Symbol('WEDNESDAY');
WeekDayEnum.THURSTDAY = Symbol('THURSTDAY');
WeekDayEnum.FRIDAY = Symbol('FRIDAY');
WeekDayEnum.SATURDAY = Symbol('SATURDAY');
WeekDayEnum.SUNDAY = Symbol('SUNDAY');
