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
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.MINUTE)
    };
  }

  resolveIntervalToCron() {
    const { intervalType } = this.state;
    switch (intervalType) {
      case 'MONTH':
        return this.resolveEveryMonth();
      case 'WEEK':
        return this.resolveEveryWeek();
      case 'DAY':
        return this.resolveEveryDay();
      case 'HOUR':
        return this.resolveHours();
      case 'MINUTE':
        return this.resolveMinutes();
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

  getContentKey() {
    // return 'content.scheduler.schedule-tasks';
    // return 'content.scheduler.schedule-tasks.entity.SchedulerTask.trigger';
  }

  onChangeIntervalType(intervalType) {
    this.setState({
      intervalType: intervalType.value
    });
  }

  getBody() {
    const { showLoading, rendered } = this.props;
    const { intervalType } = this.state;
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
          <Basic.Col lg={ 4 }>
            <div>
              { this.i18n('entity.SchedulerTask.trigger.repeat.label') }
            </div>
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
          <Basic.Col lg={ 4 }>
            <Basic.DateTimePicker
              ref="dayTime"
              mode="time"
              hidden={ intervalType !== 'DAY' && intervalType !== 'HOUR' && intervalType !== 'MINUTE' }/>
          </Basic.Col>
        </Basic.Row>

        {/* Week properties */}
        <Basic.Row
          hidden={ intervalType !== 'WEEK' }>
          <Basic.Col lg={ 2 }>
            <div>
              { 'Každé' }
            </div>
          </Basic.Col>
          <Basic.Col lg={ 2 }>
            <Basic.EnumSelectBox
              ref="weekDay"
              enum={ WeekDayEnum }
              value={ WeekDayEnum.MONDAY }
              clearable={ false }/>
          </Basic.Col>
          <Basic.Col lg={ 1 }>
            <div>
              { 'v' }
            </div>
          </Basic.Col>
          <Basic.Col lg={ 4 }>
            <Basic.DateTimePicker
              ref="weekTime"
              mode="time"/>
          </Basic.Col>
        </Basic.Row>

        {/* Month properties */}
        <Basic.Row
          hidden={ intervalType !== 'MONTH' }>
          <Basic.Col lg={ 2 }>
            <div>
              { 'Měsíčně' }
            </div>
          </Basic.Col>
          <Basic.Col lg={ 2 }>
            <Basic.EnumSelectBox
              ref="monthDay"
              enum={ DayInMonthEnum }
              value={ DayInMonthEnum.ONE }
              clearable={ false }/>
          </Basic.Col>
          <Basic.Col lg={ 2 }>
            <div>
              { 'den v' }
            </div>
          </Basic.Col>
          <Basic.Col lg={ 4 }>
            <Basic.DateTimePicker
              ref="monthTime"
              mode="time"
              // required
            />
          </Basic.Col>
        </Basic.Row>

        <Basic.Row>
          <Basic.Col lg={ 2 }>
            <div>
              { this.i18n('entity.SchedulerTask.trigger.repeat.firstRun') }
            </div>
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

      </Basic.AbstractForm>
    );
  }
}

export default CronGenerator;
