import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * Scheduler agenda entry point
 *
 * @author Radek Tomiška
 */
export default class SchedulerRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.scheduler';
  }

  render() {
    console.log();
    return (
      <div>
        { this.renderPageHeader({ icon: 'fa:calendar-times-o'}) }

        <Advanced.TabPanel position="top" parentId="scheduler" params={this.props.params}>
          {this.props.children}
        </Advanced.TabPanel>
      </div>
    );
  }
}
