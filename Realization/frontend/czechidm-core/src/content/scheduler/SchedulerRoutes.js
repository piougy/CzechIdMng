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
    return (
      <div>
        { this.renderPageHeader({ icon: 'component:scheduled-tasks'}) }

        <Advanced.TabPanel position="top" parentId="scheduler" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </div>
    );
  }
}
