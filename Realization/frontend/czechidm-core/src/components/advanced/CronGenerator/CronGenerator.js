import React from 'react';
import * as Basic from '../../basic';
//
import AbstractFormComponent from '../../basic/AbstractFormComponent/AbstractFormComponent';
import IntervalTypeEnum from '../../../enums/IntervalTypeEnum';
import WeekDayEnum from '../../../enums/WeekDayEnum';
import CronMinuteEnum from '../../../enums/CronMinuteEnum';
import CronHourEnum from '../../../enums/CronHourEnum';
import DayInMonthEnum from '../../../enums/DayInMonthEnum';
// import Datetime from 'react-datetime';

class CronGenerator extends AbstractFormComponent {

  constructor(props) {
    super(props);
    this.state = {
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.MINUTE),
      cronExpression: this.resolveIntervalToCron()
    };
  }

  getComponentKey() {
    // jak se dostane lokalizace do: 'content.scheduler.schedule-tasks' ???
    return 'entity.SchedulerTask.trigger.repeat';
  }

  onChangeIntervalType(intervalType) {
    this.setState({
      intervalType: intervalType.value,
      cronExpression: this.resolveIntervalToCron()
    });
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

  resolveMinutes() {
    // Minutes
    const time = this.refs.dayTime.getValue();
    const initMinute = time.split(':')[1];
    const repetitionRate = this.refs.cronMinute.getValue().description;

    // nahradit konstantou!
    let cronStartMinute = initMinute;
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
    // Hours
    const time = this.refs.dayTime.getValue();
    const initHour = time.split(':')[0];
    const initMinute = time.split(':')[1];

    const repetitionRate = this.refs.cronHour.getValue().description;

    // nahradit konstantou!
    let cronStartHour = initHour;
    if (initHour - repetitionRate > 0) {
      while (cronStartHour - repetitionRate > 0) {
        cronStartHour -= repetitionRate;
      }
    }

    const second = 0;
    const minute = initMinute;
    const hour = `${cronStartHour}/${repetitionRate}`;
    const monthDay = `*`;
    const monthInYear = `*`;
    const dayOfWeek = `?`;

    return `${second} ${minute} ${hour} ${monthDay} ${monthInYear} ${dayOfWeek}`;
  }

  resolveEveryDay() {
    // Day
    const time = this.refs.dayTime.getValue();
    const initHour = time.split(':')[0];
    const initMinute = time.split(':')[1];

    const second = 0;
    const minute = initMinute;
    const hour = initHour;
    const monthDay = '*';
    const monthInYear = '*';
    const dayOfWeek = '?';

    return `${second} ${minute} ${hour} ${monthDay} ${monthInYear} ${dayOfWeek}`;
  }

  resolveEveryWeek() {
    // Week
    const time = this.refs.weekTime.getValue();
    const initHour = time.split(':')[0];
    const initMinute = time.split(':')[1];

    const weekDay = this.refs.weekDay.getValue().description;

    const second = 0;
    const minute = initMinute;
    const hour = initHour;
    const monthDay = '?';
    const monthInYear = '*';
    const dayOfWeek = weekDay;

    return `${second} ${minute} ${hour} ${monthDay} ${monthInYear} ${dayOfWeek}`;
  }

  resolveEveryMonth() {
    // Month
    const time = this.refs.monthTime.getValue();
    const initHour = time.split(':')[0];
    const initMinute = time.split(':')[1];
    const dayInMonth = this.refs.monthDay.getValue().description;

    const second = 0;
    const minute = initMinute;
    const hour = initHour;
    const monthDay = dayInMonth;
    const monthInYear = '*';
    const dayOfWeek = '?';

    return `${second} ${minute} ${hour} ${monthDay} ${monthInYear} ${dayOfWeek}`;
  }

  getBody() {
    // this.generateCron();

    const { showLoading, rendered } = this.props;
    const { intervalType, cronExpression } = this.state;
    if (!rendered) {
      return null;
    }
    if (showLoading) {
      return (
        <Basic.Loading isStatic showLoading/>
      );
    }
    return (
      <Basic.AbstractForm showLoading={showLoading}>

        <Basic.Row>
          <Basic.Col lg={ 5 }>
              <h3 className="align-middle">
                { this.i18n('label') }
              </h3>
          </Basic.Col>
          <Basic.Col lg={ 2 }>
            <Basic.EnumSelectBox
              ref="cronMinute"
              enum={ CronMinuteEnum }
              value={ CronMinuteEnum.FIVE}
              clearable={ false }
              hidden={ intervalType !== 'MINUTE'}
              />
            <Basic.EnumSelectBox
              ref="cronHour"
              enum={ CronHourEnum }
              value={ CronHourEnum.ONE }
              clearable={ false }
              hidden={ intervalType !== 'HOUR'}
              />
          </Basic.Col>
          <Basic.Col lg={ 3 }>
            <Basic.EnumSelectBox
              ref="intervalType"
              enum={ IntervalTypeEnum }
              value={ intervalType }
              clearable={ false }
              onChange={this.onChangeIntervalType.bind(this)}/>
          </Basic.Col>
        </Basic.Row>

        <Basic.Row
          hidden={ intervalType !== 'DAY' && intervalType !== 'HOUR' && intervalType !== 'MINUTE' }>
          <Basic.Col lg={ 1 }>
            <h3>
              { this.i18n('at') }
            </h3>
          </Basic.Col>
          <Basic.Col lg={ 4 }>
            <Basic.DateTimePicker
              ref="dayTime"
              mode="time"
              value={ '0001-01-01 22:00' }/>
          </Basic.Col>
        </Basic.Row>

        {/* Week properties */}
        <Basic.Row
          hidden={ intervalType !== 'WEEK' }>
          <Basic.Col lg={ 2 }>
            <h3>
              { this.i18n('every') }
            </h3>
          </Basic.Col>
          <Basic.Col lg={ 2 }>
            <Basic.EnumSelectBox
              ref="weekDay"
              enum={ WeekDayEnum }
              value={ WeekDayEnum.MONDAY }
              clearable={ false }/>
          </Basic.Col>
          <Basic.Col lg={ 1 }>
            <h3>
            { this.i18n('at') }
            </h3>
          </Basic.Col>
          <Basic.Col lg={ 4 }>
            <Basic.DateTimePicker
              ref="weekTime"
              mode="time"
              value={ '0001-01-01 22:00' }
              // required
              />
          </Basic.Col>
        </Basic.Row>

        {/* Month properties */}
        <Basic.Row
          hidden={ intervalType !== 'MONTH' }>
          <Basic.Col lg={ 2 }>
            <h3>
              { this.i18n('monthly') }
            </h3>
          </Basic.Col>
          <Basic.Col lg={ 2 }>
            <Basic.EnumSelectBox
              ref="monthDay"
              enum={ DayInMonthEnum }
              value={ DayInMonthEnum.ONE }
              clearable={ false }/>
          </Basic.Col>
          <Basic.Col lg={ 1 }>
            <h3>
              { this.i18n('day') }
            </h3>
          </Basic.Col>
          <Basic.Col lg={ 1 }>
            <h3>
              { this.i18n('at') }
            </h3>
          </Basic.Col>
          <Basic.Col lg={ 4 }>
            <Basic.DateTimePicker
              ref="monthTime"
              mode="time"
              value={ '0001-01-01 22:00' }
              // required
            />
          </Basic.Col>
        </Basic.Row>

        {/* Prepared for scheduled first start */}
        <Basic.Row rendered={ false }>
          <Basic.Col lg={ 2 }>
            <h3>
              { this.i18n('validFrom') }
            </h3>
          </Basic.Col>
          <Basic.Col lg={ 5 }>
            <Basic.DateTimePicker
              ref="fireTime"
              hidden={ intervalType !== 'MINUTE' && intervalType !== 'HOUR' && intervalType !== 'DAY' }
              />
            <Basic.DateTimePicker
              ref="fireTime"
              hidden={ intervalType !== 'WEEK' && intervalType !== 'MONTH' }
              mode="date"
              />
          </Basic.Col>
        </Basic.Row>

        <Basic.Row>
          <Basic.Col lg={ 6 }>
            <h3>
            { this.i18n('cronExpression') }
            { ' ' }
            { cronExpression }
            </h3>
          </Basic.Col>
        </Basic.Row>

      </Basic.AbstractForm>
    );
  }
}

export default CronGenerator;
