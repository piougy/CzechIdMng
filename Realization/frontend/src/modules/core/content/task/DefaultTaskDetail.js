'use strict';

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import { ApprovalTaskManager } from '../../../../redux';
import { SecurityManager, IdentityManager } from '../../../../modules/core/redux';
import { ApprovalTaskService} from '../../../../services';
import * as Advanced from '../../../../components/advanced';
import ApiOperationTypeEnum from '../../../../modules/core/enums/ApiOperationTypeEnum';
import TaskStateEnum from '../../enums/TaskStateEnum';


const identityManager = new IdentityManager();
let approvalTaskManager;

class DefaultTaskDetail extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {showLoading: props.showLoading, readOnly: true};
    let {taskManager} = this.props;
    if (taskManager){
      approvalTaskManager = taskManager;
    }else {
      approvalTaskManager = new ApprovalTaskManager();
    }

  }

  componentDidMount() {
    const { taskID } = this.props;
    this.context.store.dispatch(approvalTaskManager.fetchEntityIfNeeded(taskID));
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.task != null && nextProps.task.id) {
      this.refs.approvers.setState({value: null, isLoading: true});
      approvalTaskManager.getService().getApprovers(nextProps.task.id).then(response => { return response.json()}).then((json) => {
        if (json) {
          if (!json.error && this.refs.approvers) {
            this.refs.approvers.setState({value: json, isLoading: false});
            //check if is logged user in approvers
            this.setState({readOnly:!this._hasEditRight(json)});
          }
        }
      });
      if (this.props.task !== nextProps.task){
        if (nextProps.task.userName){
          this.context.store.dispatch(identityManager.fetchEntityIfNeeded(nextProps.task.userName));
        }
        this.refs['default-form'].setData(nextProps.task);
      }
    }
  }

  getContentKey() {
    return 'content.task.DefaultTaskDetail';
  }

  _resolveTask(approve = true){
    this.refs.confirm.show(approve ? this.i18n('messages.confirmTaskApprove'):this.i18n('messages.confirmTaskDisapprove'),approve ?
    this.i18n('messages.confirmTaskApproveTitle') :  this.i18n('messages.confirmTaskDisapproveTitle')).then(result => {
      if (!this.refs['default-form'].isFormValid()) {
        return;
      }
      const task = this.refs['default-form'].getData();
      this.setState({
        showLoading: true
      }, this.refs['default-form'].processStarted());
      const { taskID } = this.props;
      let promises;
      if (approve){
        promises = approvalTaskManager.getService().approve(taskID, task);
      } else {
        promises = approvalTaskManager.getService().disapprove(taskID, task);
      }
      promises.then(response => {
        this.setState({
          showLoading: false
        },this.refs['default-form'].processEnded());
        return response.json();
      }).then((json) => {
        if (json) {
          if (!json.error) {
            this.context.store.dispatch(approvalTaskManager.fetchEntity(taskID));
            if (approve) {
              this.addMessage({ level: 'success', key: 'form-success', message: this.i18n('messages.approved')});
            } else {
              this.addMessage({ level: 'success', key: 'form-success', message: this.i18n('messages.disapproved')});
            }
            this.context.router.goBack();
          } else {
            this.refs['default-form'].setData(null, json.error, ApiOperationTypeEnum.UPDATE);
          }
        }
      }).catch(ex => {
        this.refs['default-form'].setData(null, ex, ApiOperationTypeEnum.UPDATE);
      });
    });
  }

  /**
  * Check if is logged user in approvers for this task
  * @param  {[array]}  approvers [array of users]
  * @return {Boolean}
  */
  _hasEditRight(approvers) {
    return approvalTaskManager.hasEditRight(approvers);
  }

  render() {
    const { taskID, task, applicant, readOnly, customDetailFunc} = this.props;
    const { showLoading, approvers} = this.state;
    let showLoadingInternal = task ? showLoading : true;
    let taskState = task ? task.taskState : null;
    let readOnlyInternal = readOnly || this.state.readOnly;
    if (taskState){
      readOnlyInternal = TaskStateEnum.findSymbolByKey(taskState) === TaskStateEnum.PENDING ? readOnlyInternal : true;
    }

    return (
      <div>
        <Helmet title={this.i18n('navigation.menu.task')} />
        <Basic.Confirm ref="confirm"/>
        <Basic.PageHeader>{approvalTaskManager.getNiceLabel(task)}
          <small> {this.i18n('taskDetail')}</small>
        </Basic.PageHeader>
        <Basic.Panel showLoading = {showLoadingInternal}>
          <Basic.PanelHeader text={<span>{approvalTaskManager.getNiceLabel(task)} <small>this.i18n('taskDetail')</small></span>} className="hidden">
          </Basic.PanelHeader>
          <Basic.AbstractForm ref="default-form">
            <Basic.LabelWrapper readOnly ref="taskState" label={this.i18n('taskState')} componentSpan="col-sm-3">
              <div style={{marginTop: '5px'}}>
                <Basic.Label level={TaskStateEnum.getLevel(taskState)} text = {TaskStateEnum.getNiceLabel(taskState)} className="label-form"/>
              </div>
            </Basic.LabelWrapper>
            {customDetailFunc ? customDetailFunc() : null}
            <Basic.LabelWrapper readOnly ref="applicant" label={this.i18n('applicant')} componentSpan="col-sm-5">
              <Advanced.IdentityInfo identity={applicant} showLoading={!applicant} className="no-margin"/>
            </Basic.LabelWrapper>
            <Basic.DateTimePicker readOnly ref="createdDate" label={this.i18n('createdDate')}/>
            <Basic.TextArea ref="note" readOnly={readOnlyInternal} label={this.i18n('note')}/>
            <Basic.SelectBox ref="approvers" label={this.i18n('approvers')} service={identityManager.getService()} searchInFields={['lastName', 'name','email']}
              multiSelect={true} readOnly/>
          </Basic.AbstractForm>
          <Basic.PanelFooter>
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}
              showLoading={showLoadingInternal}>{this.i18n('button.back')}</Basic.Button>
            <Basic.Button type="button" level="danger"onClick={this._resolveTask.bind(this,false)}
              showLoading={showLoadingInternal} disabled={readOnlyInternal}>{this.i18n('disapprove')}
            </Basic.Button>{' '}
            <Basic.Button type="button" level="success" onClick={this._resolveTask.bind(this,true)}
              showLoading={showLoadingInternal} disabled={readOnlyInternal}>{this.i18n('approve')}
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </div>
    )
  }
}

DefaultTaskDetail.propTypes = {
  task: PropTypes.object,
  readOnly: PropTypes.bool,
  customDetailFunc: PropTypes.func //For custom fields ... etc. for role name in RoleApprovalTaskDetail
}
DefaultTaskDetail.defaultProps = {
  task: null,
  readOnly: false
}

function select(state, component) {

  const { taskID } = component;
  if (approvalTaskManager){
    let task = approvalTaskManager.getEntity(state, taskID);
    let applicant = null;
    if (task){
      applicant = identityManager.getEntity(state, task.userName);
    }

    return {
      applicant: applicant,
      task: task
    }
  }
  return {};
}

export default connect(select)(DefaultTaskDetail);
