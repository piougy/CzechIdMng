import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import { LongRunningTaskManager } from '../../redux';
import LongRunningTaskTable from './LongRunningTaskTable';

const UIKEY = 'all-long-running-task-table';
const manager = new LongRunningTaskManager();

/**
 * All task table with FilterButtons
 *
 * @author Radek Tomiška
 */
export default class AllTasks extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.scheduler.all-tasks';
  }

  componentDidMount() {
    this.selectNavigationItems(['system', 'scheduler', 'scheduler-all-tasks']);
  }

  render() {
    //
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <LongRunningTaskTable manager={manager} uiKey={UIKEY}/>
      </div>
    );
  }
}
