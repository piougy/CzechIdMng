import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import DecisionButtons from '../DecisionButtons';
import DynamicTaskDetail from '../DynamicTaskDetail';
import RoleRequestDetail from '../../requestrole/RoleRequestDetail';
import { connect } from 'react-redux';
import SearchParameters from '../../../domain/SearchParameters';

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
  }

  _completeTask(decision) {
    const formDataValues = this.refs.formData.getData();
    const task = this.refs.form.getData();
    const formDataConverted = this._toFormData(formDataValues, task.formData);
    this.setState({
      showLoading: true
    });
    const formData = {'decision': decision.id, 'formData': formDataConverted};
    const { taskManager} = this.props;
    this.context.store.dispatch(taskManager.completeTask(task, formData, this.props.uiKey, this._afterComplete.bind(this)));
  }

  render() {
    const {task, taskManager} = this.props;
    const { showLoading} = this.state;
    const showLoadingInternal = task ? showLoading : true;
    let force = new SearchParameters();
    if (task) {
      force = force.setFilter('username', task.applicant);
    }
    const formDataValues = this._toFormDataValues(task.formData);

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm"/>

        <Basic.PageHeader>{task.taskName}
          <small> {this.i18n('header')}</small>
        </Basic.PageHeader>

        <Basic.Panel showLoading = {showLoadingInternal}>
          <Basic.PanelHeader text={<span>{taskManager.getNiceLabel(task)} <small>this.i18n('taskDetail')</small></span>} className="hidden"/>
          <Basic.AbstractForm className="panel-body" ref="form" data={task}>
            <Basic.TextField ref="taskDescription" readOnly label={this.i18n('description')}/>
            <Basic.LabelWrapper readOnly ref="applicant" label={this.i18n('applicant')}>
              <Advanced.IdentityInfo username={task.applicant} showLoading={!task} className="no-margin"/>
            </Basic.LabelWrapper>
            <Basic.LabelWrapper rendered={task.variables.implementerIdentifier} readOnly ref="implementerIdentifier" label={this.i18n('implementerIdentifier')}>
              <Advanced.IdentityInfo entityIdentifier ={task.variables.implementerIdentifier} showLoading={!task} className="no-margin"/>
            </Basic.LabelWrapper>
            <Basic.DateTimePicker ref="taskCreated" readOnly label={this.i18n('createdDate')}/>
          </Basic.AbstractForm>
        </Basic.Panel>
        <Basic.Panel showLoading = {showLoadingInternal}>
          <Basic.AbstractForm ref="formData" data={formDataValues} style={{ padding: '15px 15px 0px 15px' }}>
            {this._getFormDataComponents(task)}
          </Basic.AbstractForm>
          <Basic.PanelFooter>
            <DecisionButtons task={task} onClick={this._validateAndCompleteTask.bind(this)}/>
          </Basic.PanelFooter>
        </Basic.Panel>
        <RoleRequestDetail
          ref="identityRoleConceptTable"
          uiKey="identity-role-concept-table"
          entityId={task.variables.entityEvent.content.id}
          adminMode={false}
          showRequestDetail={false}
          editableInStates={['IN_PROGRESS']}
          showLoading={showLoadingInternal}/>
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
      _showLoading: false
    };
  }
  return {
    _showLoading: true
  };
}

export default connect(select, null, null, { withRef: true })(DynamicTaskRoleDetail);
