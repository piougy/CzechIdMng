

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../../../components/basic';
import * as Advanced from '../../../../../components/advanced';
import DecisionButtons from '../DecisionButtons';
import DynamicTaskDetail from '../DynamicTaskDetail';
import IdentityRoleConceptTable from './IdentityRoleConceptTable';
import {IdentityRoleManager} from '../../../redux';
import { connect } from 'react-redux';
import SearchParameters from '../../../domain/SearchParameters';

const identityRoleManager = new IdentityRoleManager();
const uiKey = 'identity-roles-concept-task';

/**
 * Custom task detail designed for use with RoleConceptTable.
 * Extended from DynamicTaskDetail (it is standard task detail renderer)
 */
class DynamicTaskRoleDetail extends DynamicTaskDetail {

  getContentKey() {
    return 'content.task.instance';
  }

  componentDidMount() {
    super.componentDidMount();
    const {task} = this.props;
    if (task) {
      this.context.store.dispatch(identityRoleManager.fetchRoles(task.applicant, `${uiKey}-${task.applicant}`));
    }
  }

  _completeTask(decision) {
    const formDataValues = this.refs.formData.getData();
    const task = this.refs.form.getData();
    const conceptTable = this.refs.identityRoleConceptTable;
    const addedIdentityRoles = conceptTable.getAddedIdentityRoles();
    const removedIdentityRoles = conceptTable.getRemovedIdentityRolesIds();
    const changedIdentityRoles = conceptTable.getChangedIdentityRoles();
    const formDataConverted = this._toFormData(formDataValues, task.formData);
    const taskVariables = {};
    taskVariables.addedIdentityRoles = addedIdentityRoles;
    taskVariables.removedIdentityRoles = removedIdentityRoles;
    taskVariables.changedIdentityRoles = changedIdentityRoles;

    const formData = {'decision': decision.id, 'formData': formDataConverted, 'variables': taskVariables};
    const { taskManager} = this.props;
    this.context.store.dispatch(taskManager.completeTask(task, formData, this.props.uiKey, this._afterComplete.bind(this)));
  }

  render() {
    const {task, taskManager, _currentIdentityRoles} = this.props;
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
        <Basic.PageHeader>{task.taskName}
          <small> {this.i18n('header')}</small>
        </Basic.PageHeader>
        <Basic.Panel showLoading = {showLoadingInternal}>
          <Basic.PanelHeader text={<span>{taskManager.getNiceLabel(task)} <small>this.i18n('taskDetail')</small></span>} className="hidden">
          </Basic.PanelHeader>
          <Basic.AbstractForm ref="form" className="form-horizontal">
            <Basic.TextField ref="taskDescription" readOnly label={this.i18n('description')}/>
            <Basic.LabelWrapper readOnly ref="applicant" label={this.i18n('applicant')} componentSpan="col-sm-5">
              <Advanced.IdentityInfo username={task.applicant} showLoading={!task} className="no-margin"/>
            </Basic.LabelWrapper>
            <Basic.DateTimePicker ref="taskCreated" readOnly label={this.i18n('createdDate')}/>
          </Basic.AbstractForm>
        </Basic.Panel>
        <Basic.Panel showLoading = {showLoadingInternal}>
          <Basic.PanelHeader text={<small>{this.i18n('content.task.instance.role.conceptIdentityRoles')}</small>}/>
          <IdentityRoleConceptTable
            ref="identityRoleConceptTable"
            uiKey="identity-role-concept-table"
            identityUsername={task.applicant}
            identityRoles={_currentIdentityRoles}
            addedIdentityRoles={task.variables.addedIdentityRoles}
            changedIdentityRoles={task.variables.changedIdentityRoles}
            removedIdentityRoles={task.variables.removedIdentityRoles}
            />
        </Basic.Panel>
        <Basic.Panel showLoading = {showLoadingInternal}>
          <Basic.AbstractForm ref="formData" className="form-horizontal">
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
function select(state, component) {
  const task = component.task;
  if (task) {
    return {
      _showLoading: identityRoleManager.isShowLoading(state, `${uiKey}-${task.applicant}`),
      _currentIdentityRoles: identityRoleManager.getEntities(state, `${uiKey}-${task.applicant}`)
    };
  }
  return {
    _showLoading: true
  };
}

export default connect(select, null, null, { withRef: true })(DynamicTaskRoleDetail);
