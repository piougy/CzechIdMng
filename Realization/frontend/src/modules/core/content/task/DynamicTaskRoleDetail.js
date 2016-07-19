

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import DecisionButtons from './DecisionButtons';
import DynamicTaskDetail from './DynamicTaskDetail';
import UserRoleTable from '../user/UserRoleTable';
import {IdentityRoleManager} from '../../redux';
import SearchParameters from '../../domain/SearchParameters';

const identityRoleManager = new IdentityRoleManager();

class DynamicTaskRoleDetail extends DynamicTaskDetail {

  getContentKey() {
    return 'content.task.instance';
  }

  render() {
    const {task, taskManager} = this.props;
    const { showLoading} = this.state;
    const showLoadingInternal = task ? showLoading : true;
    let force = new SearchParameters();
    if (task) {
      force = force.setFilter('username', task.applicant);
    }
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm"/>
        <Basic.PageHeader>{task.name}
          <small> {this.i18n('header')}</small>
        </Basic.PageHeader>
        <Basic.Panel showLoading = {showLoadingInternal}>
          <Basic.PanelHeader text={<span>{taskManager.getNiceLabel(task)} <small>this.i18n('taskDetail')</small></span>} className="hidden">
          </Basic.PanelHeader>
          <Basic.AbstractForm ref="form">
            <Basic.TextField ref="taskName" readOnly label={this.i18n('name')}/>
            <Basic.TextField ref="taskDescription" readOnly label={this.i18n('description')}/>
            <Basic.LabelWrapper readOnly ref="applicant" label={this.i18n('applicant')} componentSpan="col-sm-5">
              <Advanced.IdentityInfo username={task.applicant} showLoading={!task} className="no-margin"/>
            </Basic.LabelWrapper>
            <Basic.DateTimePicker ref="taskCreated" readOnly label={this.i18n('createdDate')}/>
          </Basic.AbstractForm>
        </Basic.Panel>
        <Basic.Panel showLoading = {showLoadingInternal}>
          <Basic.PanelHeader text={<small>{this.i18n('content.task.instance.role.currentRoles')}</small>}/>
          <UserRoleTable
            uiKey="user-role-table"
            forceSearchParameters={force}
            identityRoleManager={identityRoleManager}/>
        </Basic.Panel>
        <Basic.Panel showLoading = {showLoadingInternal}>
          <Basic.AbstractForm ref="formData">
            {this._getFormDataComponents(task)}
          </Basic.AbstractForm>
          <Basic.PanelFooter>
            <DecisionButtons task={task} onClick={this._validateAndCompleteTask.bind(this)}/>
          </Basic.PanelFooter>
        </Basic.Panel>
      </div>
    );
  }
}

DynamicTaskRoleDetail.propTypes = {
  task: PropTypes.object,
  readOnly: PropTypes.bool,
  taskManager: PropTypes.object.isRequired
};

DynamicTaskRoleDetail.defaultProps = {
  task: null,
  readOnly: false
};

export default DynamicTaskRoleDetail;
