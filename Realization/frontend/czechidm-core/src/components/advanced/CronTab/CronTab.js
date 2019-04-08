
import React, { PropTypes } from 'react';
import * as Basic from '../../basic';
//
import AbstractComponent from '../../basic/AbstractComponent/AbstractComponent';
// import Datetime from 'react-datetime';

class CronTab extends AbstractComponent {

  getContentKey() {
    return 'content.scheduler.schedule-tasks';
  }

  constructor(props) {
    super(props);
  }

    render() {
      const { style, showLoading, rendered, hidden, required } = this.props;
      if (!rendered) {
        return null;
      }
      if (showLoading) {
        return (
          <Basic.Loading isStatic showLoading/>
        );
      }
      return (
        // posunout hidden do ScheduleTasks
        <div
          hidden={ hidden }>
          <Basic.DateTimePicker
            ref="fireTime"
            label={ "ahoj" }
            // hidden={ hidden }
            // required={ triggerType === 'ADVANCED' }
            />
          <Basic.DateTimePicker
            ref="fireTime"
            // label={ this.i18n('entity.SchedulerTask.trigger.fireTime') }
            // hidden={ hidden }
            // required={ triggerType === 'ADVANCED' }
            />
        </div>
      );
    }
}

export default CronTab;
