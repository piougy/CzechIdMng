import React, { PropTypes } from 'react';
import * as Basic from '../../basic';
//
import AbstractFormComponent from '../../basic/AbstractFormComponent/AbstractFormComponent';
import IntervalTypeEnum from '../../../enums/IntervalTypeEnum';
import Row from '../../basic/Row/Row';
// import Datetime from 'react-datetime';

class CronGenerator extends AbstractFormComponent {

  constructor(props) {
    super(props);
    this.state = {
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.HOUR)
    };
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
    const { style, showLoading, rendered, label } = this.props;
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
          <Basic.Col lg={ 6 }>
            <div>
              { this.i18n('entity.SchedulerTask.trigger.repeat.label') }
            </div>
          </Basic.Col>
          <Basic.Col lg={ 3 }>
            <Basic.EnumSelectBox
              ref="intervalType"
              enum={ IntervalTypeEnum }
              // required
              onChange={this.onChangeIntervalType.bind(this)}/>
          </Basic.Col>
          <Basic.Col lg={ 3 }>
            <Basic.EnumSelectBox
              ref="intervalType"
              enum={ IntervalTypeEnum }
              // required
              onChange={this.onChangeIntervalType.bind(this)}/>
          </Basic.Col>
        </Basic.Row>

        <Basic.DateTimePicker
          ref="fireTime"
          label={ this.i18n('entity.SchedulerTask.trigger.repeat.firstRun') }
          hidden={ intervalType !== 'MINUTE' && intervalType !== 'HOUR' && intervalType !== 'DAY' }
          required={ intervalType !== 'MINUTE' && intervalType !== 'HOUR' && intervalType !== 'DAY' }
          />
      </Basic.AbstractForm>
    );
  }
}

export default CronGenerator;
