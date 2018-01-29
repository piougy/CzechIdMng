import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
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

  componentDidMount() {
    this.selectNavigationItems(['tasks', 'tasks-identity']);
  }

  getContentKey() {
    return 'content.tasks.identity';
  }

  getNavigationKey() {
    return 'tasks';
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <TaskInstanceTable uiKey="task-instance-table" taskInstanceManager={this.workflowTaskInstanceManager} filterOpened={false}/>
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
