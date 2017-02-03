import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

export default class SchedulerRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.scheduler';
  }

  render() {
    return (
      <div>
        {this.renderPageHeader()}

        <Advanced.TabPanel position="top" parentId="scheduler" params={this.props.params}>
          {this.props.children}
        </Advanced.TabPanel>
      </div>
    );
  }
}
