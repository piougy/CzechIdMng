

import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import { WorkflowTaskInstanceManager } from '../../redux';
import TaskInstanceTable from './TaskInstanceTable';

/**
 * List of instances tasks
 */
class TaskInstances extends Basic.AbstractContent {

  constructor(props, context) {
     super(props, context);
     this.workflowTaskInstanceManager = new WorkflowTaskInstanceManager();
  }

  getContentKey() {
    return 'content.task.instances';
  }

  componentDidMount() {
    this.selectNavigationItem('tasks');
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <TaskInstanceTable uiKey='task_instance_table' taskInstanceManager={this.workflowTaskInstanceManager} filterOpened={false}/>
        </Basic.Panel>

      </div>
    );
  }
}

TaskInstances.propTypes = {
}
TaskInstances.defaultProps = {
}

function select(state) {
  return {
  }
}

export default connect(select)(TaskInstances)
