import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
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

  getNavigationKey() {
    return 'tasks';
  }

  render() {
    return (
      <div>
        {this.renderPageHeader()}

        <Basic.Panel>
          <TaskInstanceTable uiKey="task-instance-table" taskInstanceManager={this.workflowTaskInstanceManager} filterOpened={false}/>
        </Basic.Panel>

      </div>
    );
  }
}

TaskInstances.propTypes = {
};
TaskInstances.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(TaskInstances);
