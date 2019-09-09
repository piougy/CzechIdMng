import React from 'react';
import TestUtils from 'react-addons-test-utils';
import moment from 'moment';
import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);

import * as Advanced from '../../../../src/components/advanced/';
import IntervalTypeEnum from '../../../../src/enums/IntervalTypeEnum';
import WeekDayEnum from '../../../../src/enums/WeekDayEnum';
import CronMinuteEnum from '../../../../src/enums/CronMinuteEnum';
import CronHourEnum from '../../../../src/enums/CronHourEnum';
import DayInMonthEnum from '../../../../src/enums/DayInMonthEnum';

/**
 * CronGenerator method tests
 *
 * @author Petr Hanák
 */

describe('CronGenerator', function testCronGenerator() {
  const cronGeneratorInstance = TestUtils.renderIntoDocument(<Advanced.CronGenerator/>);
  it('- resolve minutes interval to cron', function validate() {
    // every 15 minutes from 18:37
    cronGeneratorInstance.setState({
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.MINUTE),
      cronMinute: CronMinuteEnum.findKeyBySymbol(CronMinuteEnum.FIFTEEN),
      firstStartDateTime: moment().set({'hour': 18, 'minute': 37}),
    });
    const cronExpression = cronGeneratorInstance.resolveIntervalToCron();
    const cronCorrectExpression = '0 7/15 * * * ?';

    expect(cronExpression).to.equal(cronCorrectExpression);
    // test of cron expression in state
    cronGeneratorInstance.generateCron();
    expect(cronGeneratorInstance.state.cronExpression).to.equal(cronCorrectExpression);
  });
  it('- resolve hours interval to cron', function validate() {
    // every 8 hours from 10:53
    cronGeneratorInstance.setState({
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.HOUR),
      cronHour: CronHourEnum.findKeyBySymbol(CronHourEnum.EIGHT),
      firstStartDateTime: moment().set({'hour': 10, 'minute': 53}),
    });
    const cronExpression = cronGeneratorInstance.resolveIntervalToCron();
    const cronCorrectExpression = '0 53 2/8 * * ?';

    expect(cronExpression).to.equal(cronCorrectExpression);
    // test of cron expression in state
    cronGeneratorInstance.generateCron();
    expect(cronGeneratorInstance.state.cronExpression).to.equal(cronCorrectExpression);
  });
  it('- resolve every day interval to cron', function validate() {
    // every day at 14:32
    cronGeneratorInstance.setState({
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.DAY),
      firstStartDateTime: moment().set({'hour': 14, 'minute': 32}),
    });
    const cronExpression = cronGeneratorInstance.resolveIntervalToCron();
    const cronCorrectExpression = '0 32 14 * * ?';

    expect(cronExpression).to.equal(cronCorrectExpression);
    // test of cron expression in state
    cronGeneratorInstance.generateCron();
    expect(cronGeneratorInstance.state.cronExpression).to.equal(cronCorrectExpression);
  });
  it('- resolve every week interval to cron', function validate() {
    // every saturday at 02:01
    cronGeneratorInstance.setState({
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.WEEK),
      weekDay: WeekDayEnum.findKeyBySymbol(WeekDayEnum.SATURDAY),
      time: '02:01',
    });
    const cronExpression = cronGeneratorInstance.resolveIntervalToCron();
    const cronCorrectExpression = '0 1 2 ? * sat';

    expect(cronExpression).to.equal(cronCorrectExpression);
    // test of cron expression in state
    cronGeneratorInstance.generateCron();
    expect(cronGeneratorInstance.state.cronExpression).to.equal(cronCorrectExpression);
  });
  it('- resolve every week interval to cron', function validate() {
    // every saturday at 23:20
    cronGeneratorInstance.setState({
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.MONTH),
      dayInMonth: DayInMonthEnum.findKeyBySymbol(DayInMonthEnum.TWENTYEIGHT),
      time: '23:20',
    });
    const cronExpression = cronGeneratorInstance.resolveIntervalToCron();
    const cronCorrectExpression = '0 20 23 28 * ?';

    expect(cronExpression).to.equal(cronCorrectExpression);
    // test of cron expression in state
    cronGeneratorInstance.generateCron();
    expect(cronGeneratorInstance.state.cronExpression).to.equal(cronCorrectExpression);
  });
});
