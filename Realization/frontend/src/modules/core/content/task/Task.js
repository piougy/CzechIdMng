

import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import {WorkflowTaskInstanceManager } from '../../../../modules/core/redux';
import DynamicTaskDetail from './DynamicTaskDetail';
import ComponentService from '../../../../services/ComponentService';

const workflowTaskInstanceManager = new WorkflowTaskInstanceManager();
const componentService = new ComponentService();

class Task extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {showLoading: props.showLoading};
  }

  componentDidMount() {
    this.selectNavigationItem('tasks');
    const { taskID } = this.props.params;
    this.context.store.dispatch(workflowTaskInstanceManager.fetchEntityIfNeeded(taskID));
  }

  componentDidUpdate() {
    this.selectNavigationItem('tasks');
  }

  getContentKey() {
    return 'content.task';
  }

  render() {
    const { readOnly, task} = this.props;
    let DetailComponent;
    if (task && task.formKey) {
      DetailComponent = componentService.getComponent(task.formKey);
      if (!DetailComponent) {
        this.addMessage({title: this.i18n('message.task.detailNotFound'), level: 'warning'});
        this.context.router.goBack();
        return null;
      }
    }
    if (!DetailComponent) {
      DetailComponent = DynamicTaskDetail;
    }
    return (
        <div>
          {task ?
          <DetailComponent task={task} uiKey="dynamic-task-detail" taskManager={workflowTaskInstanceManager} readOnly={readOnly}/>
          :
          <Basic.Well showLoading/>
          }
        </div>
    );
  }
}

Task.propTypes = {
  task: PropTypes.object,
  readOnly: PropTypes.bool
};

Task.defaultProps = {
  task: null,
  readOnly: false
};

function select(state, component) {
  const { taskID } = component.params;
  const task = workflowTaskInstanceManager.getEntity(state, taskID);

  return {
    task
  };
}

export default connect(select)(Task);
