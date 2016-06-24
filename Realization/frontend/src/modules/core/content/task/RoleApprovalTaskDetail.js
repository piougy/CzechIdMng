'use strict';

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import { RoleApprovalTaskManager} from '../../../../redux';
import * as Advanced from '../../../../components/advanced';
import ApiOperationTypeEnum from '../../../../modules/core/enums/ApiOperationTypeEnum';
import TaskStateEnum from '../../enums/TaskStateEnum';
import DefaultTaskDetail from './DefaultTaskDetail';

const roleApprovalTaskManager = new RoleApprovalTaskManager();

class RoleApprovalTaskDetail extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {showLoading: props.showLoading};
  }

  componentDidMount() {
  }

  componentDidUpdate() {
  }

  getContentKey() {
    return 'content.task.RoleApprovalTaskDetail';
  }

  getComponent(){
    return <Basic.TextField ref="roleName" readOnly label={this.i18n('roleName') } componentSpan="col-sm-5"/>;
  }

  render() {
    const { readOnly, taskID} = this.props;
    return (
            <div>
              <DefaultTaskDetail taskID={taskID} readOnly={readOnly} taskManager={roleApprovalTaskManager} customDetailFunc={this.getComponent.bind(this)}/>
            </div>
    )
  }
}

RoleApprovalTaskDetail.propTypes = {
  task: PropTypes.object,
  readOnly: PropTypes.bool
}
RoleApprovalTaskDetail.defaultProps = {
  task: null,
  readOnly: false
}


export default RoleApprovalTaskDetail;
