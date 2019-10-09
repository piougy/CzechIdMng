import React from 'react';
import * as Basic from '../../basic';
import moment from 'moment';
//
import AbstractFormComponent from '../../basic/AbstractFormComponent/AbstractFormComponent';
import IntervalTypeEnum from '../../../enums/IntervalTypeEnum';
import WeekDayEnum from '../../../enums/WeekDayEnum';
import CronMinuteEnum from '../../../enums/CronMinuteEnum';
import CronHourEnum from '../../../enums/CronHourEnum';
import DayInMonthEnum from '../../../enums/DayInMonthEnum';
import Datetime from 'react-datetime';

class CronGenerator extends AbstractFormComponent {

  constructor(props) {
    super(props);
    this.state = {
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.MINUTE),
      weekDay: WeekDayEnum.findKeyBySymbol(WeekDayEnum.MONDAY),
      cronMinute: CronMinuteEnum.findKeyBySymbol(CronMinuteEnum.FIVE),
      cronHour: CronHourEnum.findKeyBySymbol(CronHourEnum.ONE),
      dayInMonth: DayInMonthEnum.findKeyBySymbol(DayInMonthEnum.ONE),
      time: '22:00',
      validFromDate: moment().startOf('day').format('MM.DD.YYYY'),
      firstStartDateTime: moment().startOf('minute').format('MM.DD.YYYY HH:mm'),
      cronExpression: '',
      showMonthWarning: false,
      weekDaySpelling: 'every_1'
    };
  }

  getComponentKey() {
    return 'entity.SchedulerTask.trigger.repeat';
  }

  getExecuteDateTime() {
    const { intervalType, firstStartDateTime, validFromDate } = this.state;
    return (intervalType === 'MINUTE' || intervalType === 'HOUR' || intervalType === 'DAY') ? 
    moment(firstStartDateTime).format('DD.MM.YYYY HH:mm') :
    moment(validFromDate).format('DD.MM.YYYY HH:mm');
  }

  onChangeIntervalType(intervalType) {
    this.setState({
      intervalType: intervalType.value,
      showMonthWarning: this.showWarning(intervalType.value)
    }, () => this.generateCron());
  }

  showWarning(intervalType) {
    const dayNumber = DayInMonthEnum.findSymbolByKey(this.state.dayInMonth).description;
    if (intervalType === 'MONTH' &&
      (dayNumber === '29' || dayNumber === '30' || dayNumber === '31')) {
      return true;
    }
    return false;
  }

  onChangeWeekDay(weekDay) {
    const weekDaySymbol = WeekDayEnum.findSymbolByKey(weekDay.value).description;
    this.setState({
      weekDay: weekDay.value,
      weekDaySpelling: this.checkWeekDaySpelling(weekDaySymbol)
    }, () => this.generateCron());
  }

  checkWeekDaySpelling(weekDaySymbol) {
    switch (weekDaySymbol) {
      case 'mon':
        return 'every_1';
      case 'tue':
        return 'every_1';
      case 'wed':
        return 'every_2';
      case 'thu':
        return 'every_3';
      case 'fri':
        return 'every_3';
      case 'sat':
        return 'every_2';
      case 'sun':
        return 'every_2';
      default:
      //
    }
  }

  onChangeCronMinute(cronMinute) {
    this.setState({
      cronMinute: cronMinute.value
    }, () => this.generateCron());
  }

  onChangeCronHour(cronHour) {
    this.setState({
      cronHour: cronHour.value
    }, () => this.generateCron());
  }

  onChangeDayInMonth(dayInMonth) {
    const dayNumber = DayInMonthEnum.findSymbolByKey(dayInMonth.value).description;
    this.setState({
      dayInMonth: dayInMonth.value,
      showMonthWarning: dayNumber === '29' || dayNumber === '30' || dayNumber === '31' ? true : false
    }, () => this.generateCron());
  }

  onChangeTime(time) {
    const value = moment(time, 'HH:mm').format('HH:mm');
    this.setState({
      time: value
    }, () => this.generateCron());
  }

  onChangeDate(date) {
    this.setState({
      validFromDate: date
    }, () => console.log(this.state.validFromDate));
  }

  onChangeDateTime(dateTime) {
    this.setState({
      firstStartDateTime: dateTime
    }, () => this.generateCron());
  }

  generateCron() {
    this.setState({
      cronExpression: this.resolveIntervalToCron()
    });
  }

  resolveIntervalToCron() {
    const { intervalType } = this.state;
    switch (intervalType) {
      case 'MINUTE':
        return this.resolveMinutes();
      case 'HOUR':
        return this.resolveHours();
      case 'DAY':
        return this.resolveEveryDay();
      case 'WEEK':
        return this.resolveEveryWeek();
      case 'MONTH':
        return this.resolveEveryMonth();
      default:
        // should never happened
    }
  }

  reCronNumber(number) {
    // cuts first zeros from minutes and hours
    if (number.length === 2) {
      const result = (number[0] === '0') ? number[1] : number;
      return result;
    }
    return number;
  }

  resolveMinutes() {
    const initMinute = moment(this.state.firstStartDateTime).get("minute");
    const repetitionRate = CronMinuteEnum.findSymbolByKey(this.state.cronMinute).description;

    let cronStartMinute = this.reCronNumber(initMinute);
    if (initMinute - repetitionRate > 0) {
      while (cronStartMinute - repetitionRate > 0) {
        cronStartMinute -= repetitionRate;
      }
    }

    const second = 0;
    const minute = `${cronStartMinute}/${repetitionRate}`;
    const hour = `*`;
    const monthDay = `*`;
    const monthInYear = `*`;
    const dayOfWeek = `?`;

    return `${second} ${minute} ${hour} ${monthDay} ${monthInYear} ${dayOfWeek}`;
  }

  resolveHours() {
    const { firstStartDateTime, cronHour } = this.state;
    const initHour = moment(firstStartDateTime).get("hour");
    const initMinute = moment(firstStartDateTime).get("minute");
    const repetitionRate = CronHourEnum.findSymbolByKey(cronHour).description;

    let cronStartHour = this.reCronNumber(initHour);
    if (initHour - repetitionRate > 0) {
      while (cronStartHour - repetitionRate > 0) {
        cronStartHour -= repetitionRate;
      }
    }

    const second = 0;
    const minute = this.reCronNumber(initMinute);
    const hour = `${cronStartHour}/${repetitionRate}`;
    const monthDay = `*`;
    const monthInYear = `*`;
    const dayOfWeek = `?`;

    return `${second} ${minute} ${hour} ${monthDay} ${monthInYear} ${dayOfWeek}`;
  }

  resolveEveryDay() {
    const { firstStartDateTime } = this.state;
    const initHour = moment(firstStartDateTime).get("hour");
    const initMinute = moment(firstStartDateTime).get("minute");

    const second = 0;
    const minute = this.reCronNumber(initMinute);
    const hour = this.reCronNumber(initHour);
    const monthDay = '*';
    const monthInYear = '*';
    const dayOfWeek = '?';

    return `${second} ${minute} ${hour} ${monthDay} ${monthInYear} ${dayOfWeek}`;
  }

  resolveEveryWeek() {
    const { time, weekDay } = this.state;
    const initHour = time.split(':')[0];
    const initMinute = time.split(':')[1];

    const second = 0;
    const minute = this.reCronNumber(initMinute);
    const hour = this.reCronNumber(initHour);
    const monthDay = '?';
    const monthInYear = '*';
    const dayOfWeek = WeekDayEnum.findSymbolByKey(weekDay).description;

    return `${second} ${minute} ${hour} ${monthDay} ${monthInYear} ${dayOfWeek}`;
  }

  resolveEveryMonth() {
    const { time, dayInMonth } = this.state;
    const initHour = time.split(':')[0];
    const initMinute = time.split(':')[1];

    const second = 0;
    const minute = this.reCronNumber(initMinute);
    const hour = this.reCronNumber(initHour);
    const monthDay = DayInMonthEnum.findSymbolByKey(dayInMonth).description;
    const monthInYear = '*';
    const dayOfWeek = '?';

    return `${second} ${minute} ${hour} ${monthDay} ${monthInYear} ${dayOfWeek}`;
  }

  getBody() {
    const { showLoading, rendered } = this.props;
    const {
      intervalType,
      cronExpression,
      cronMinute,
      cronHour,
      weekDay,
      dayInMonth,
      time,
      firstStartDateTime,
      validFromDate,
      showMonthWarning,
      weekDaySpelling
    } = this.state;
    if (!rendered) {
      return null;
    }
    if (showLoading) {
      return (
        <Basic.Loading isStatic showLoading/>
      );
    }
    return (
      <div className="cron-generator">
        <div
          className="main-group">
          <div
            className="text">
              {this.i18n('repeatEvery')}
          </div>
          <div>
            <Basic.EnumSelectBox
              ref="cronMinute"
              className="num-select"
              enum={ CronMinuteEnum }
              value={ cronMinute }
              clearable={ false }
              hidden={ intervalType !== 'MINUTE' }
              onChange={ this.onChangeCronMinute.bind(this) }
              />
            <Basic.EnumSelectBox
              ref="cronHour"
              className="num-select"
              enum={ CronHourEnum }
              value={ cronHour }
              clearable={ false }
              hidden={ intervalType !== 'HOUR' }
              onChange={ this.onChangeCronHour.bind(this) }
              />
          </div>
          <div>
            <Basic.EnumSelectBox
              ref="intervalType"
              className="interval-select"
              enum={ IntervalTypeEnum }
              value={ intervalType }
              clearable={ false }
              onChange={ this.onChangeIntervalType.bind(this) }
              input={ false }/>
          </div>
        </div>

        {/* Week properties */}
        {intervalType === 'WEEK' && (
          <div className="secondary-group">
            <div
              className="text">
                { this.i18n(weekDaySpelling) }
            </div>
            <div>
              <Basic.EnumSelectBox
                className="weekday-select"
                ref="weekDay"
                enum={ WeekDayEnum }
                value={ weekDay }
                clearable={ false }
                onChange={ this.onChangeWeekDay.bind(this) }/>
            </div>
            <div className="text">
              { this.i18n('at') }
            </div>
            <div className="time-select">
              <Datetime
                ref="weekTime"
                dateFormat={ false }
                timeFormat={ 'HH:mm' }
                value={ time }
                onChange={ this.onChangeTime.bind(this) }
              />
            </div>
          </div>
        )}

        {/* Month properties */}
        {intervalType === 'MONTH' && (
          <div className="secondary-group">
            <div
              className="text">
                { this.i18n('monthly') }
            </div>
            <div>
              <Basic.EnumSelectBox
                ref="monthDay"
                className="num-select"
                enum={ DayInMonthEnum }
                value={ dayInMonth }
                clearable={ false }
                onChange={ this.onChangeDayInMonth.bind(this) }
                />
            </div>
            <div className="text">
                { this.i18n('day') }
                {' '}
                { this.i18n('at') }
            </div>
            <div
              className="time-select">
              <Datetime
                ref="monthTime"
                dateFormat={ false }
                timeFormat={ 'HH:mm' }
                value={ time }
                onChange={ this.onChangeTime.bind(this) }
                />
            </div>
          </div>
        )}

        {/* Warning for 29., 30., 31. days of month */}
        {showMonthWarning && (
          <div className="text-group">
            <Basic.Icon icon="warning-sign" className="text warning-icon"/>
            {' '}
            {this.i18n('warning')}
          </div>
        )}

        {/* Prepared for scheduled first start */}
        <div className="secondary-group">
          <div className="text">
            {(intervalType === 'MINUTE' || intervalType === 'HOUR' || intervalType === 'DAY') ?
            this.i18n('firstStart') : this.i18n('validFrom')}
          </div>
          <div>
            <Basic.DateTimePicker
              className="date-select"
              ref="fireTime"
              hidden={
                intervalType !== 'MINUTE' &&
                intervalType !== 'HOUR' &&
                intervalType !== 'DAY' }
              value={ firstStartDateTime }
              onChange={ this.onChangeDateTime.bind(this) }
            />
            <Basic.DateTimePicker
              className="date-select"
              ref="fireTime"
              hidden={ intervalType !== 'WEEK' && intervalType !== 'MONTH' }
              mode="date"
              value={ validFromDate }
              onChange={ this.onChangeDate.bind(this) }
            />
          </div>
        </div>

        <div className="text-group cron-expression">
          { this.i18n('cronExpression') }
          { ' ' }
          { cronExpression }
        </div>

      </div>
    );
  }
}

export default CronGenerator;
