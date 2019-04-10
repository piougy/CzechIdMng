
import React, { PropTypes } from 'react';
import * as Basic from '../../basic';
//
import AbstractFormComponent from '../../basic/AbstractFormComponent/AbstractFormComponent';
import IntervalTypeEnum from '../../../enums/IntervalTypeEnum';
// import Datetime from 'react-datetime';

class CronTab extends AbstractFormComponent {

  getContentKey() {
    return 'content.scheduler.schedule-tasks';
  }

  constructor(props) {
    super(props);
    this.state = {
      intervalType: IntervalTypeEnum.findKeyBySymbol(IntervalTypeEnum.HOUR)
    }
  }

  onChangeIntervalType(intervalType) {
    this.setState({
      intervalType: intervalType.value
    });
  }

  getBody() {
    const { style, showLoading, rendered, label } = this.props;
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
        <Basic.EnumSelectBox
          ref="type"
          enum={ IntervalTypeEnum }
          label={ this.i18n('entity.SchedulerTask.trigger._type.label') }
          required
          onChange={this.onChangeIntervalType.bind(this)}/>
        <Basic.DateTimePicker
          ref="fireTime"
          label={ this.i18n('entity.SchedulerTask.trigger.fireTime') }
          // hidden={ intervalType !== 'HOUR' }
          // required={ intervalType === 'HOUR' }
          />
      </Basic.AbstractForm>
    );
  }
}

export default CronTab;
