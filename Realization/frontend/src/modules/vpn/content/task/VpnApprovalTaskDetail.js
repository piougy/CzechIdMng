'use strict';

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import { ApprovalTaskManager } from '../../../../redux';
import { SecurityManager, IdentityManager } from '../../../../modules/core/redux';
import { AuthenticateService } from '../../../../modules/core/services';
import { ApprovalTaskService } from '../../../../services';
import { VpnApprovalTaskManager, VpnRecordManager} from '../../redux/data';
import * as Advanced from '../../../../components/advanced';
import ApiOperationTypeEnum from '../../../../modules/core/enums/ApiOperationTypeEnum';
import TaskStateEnum from '../../../core/enums/TaskStateEnum';
import VpnRecordDetail from '../VpnRecordDetail';
import VpnRecordProfileDetail from '../VpnRecordProfileDetail';


const identityManager = new IdentityManager();
let approvalTaskManager = new VpnApprovalTaskManager();
let vpnRecordManager = new VpnRecordManager();

class VpnApprovalTaskDetail extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    const {readOnly} = this.props;
    this.state = {showLoading: false, disapprovePublicNote: null, readOnly: true};
  }

  componentDidMount() {
    const { taskID, task} = this.props;
    this.context.store.dispatch(approvalTaskManager.fetchEntityIfNeeded(taskID));
    if (task){
      this.refs['default-form'].setData(task);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.task != null && nextProps.task.id) {
      if (!this.state.approvers){
        this.refs.approvers.setState({value: null, isLoading: true});
        approvalTaskManager.getService().getApprovers(nextProps.task.id).then(response => { return response.json()}).then((json) => {
          if (json) {
            if (!json.error && this.refs.approvers) {
              this.refs.approvers.setState({value: json, isLoading: false});
              //check if is logged user in approvers
              this.setState({readOnly:!this._hasEditRight(json), approvers:json});
            }
          }
        });
      }
      if (this.props.task !== nextProps.task){
        this.refs['default-form'].setData(nextProps.task);
      }
    }
  }

  getContentKey() {
    return 'vpn:content.VpnApprovalTaskDetail';
  }

  /**
  * Call from disapprove confirm dialog. Validate and manages note field.
  */
  _disapproveConfirm(action){
    if (action === 'confirm'){
      if (!this.refs.disapprovePublicNote.validate(true)){
        return false;
      }
      this.setState({disapprovePublicNote: this.refs.disapprovePublicNote.getValue()});
    }
    return true;
  }

  /**
  * Resolve task (call task api)
  * @param  {[boolean]} approve = true [task will approve or disapprove]
  */
  _resolveTask(approve = true){
    if (!this.refs['default-form'].isFormValid() || !this.refs.recordDetail.isFormValid()) {
      return;
    }
    //Set actual publicNote as default disapprovePublicNote
    this.setState({disapprovePublicNote: this.refs.recordDetail.getData().currentActivity.publicNote});
    let confirm = approve ? 'confirm' : 'disapproveConfirm';
    this.refs[confirm].show(approve ? this.i18n('messages.confirmTaskApprove'):this.i18n('messages.confirmTaskDisapprove'),approve ?
    this.i18n('messages.confirmTaskApproveTitle') :  this.i18n('messages.confirmTaskDisapproveTitle'), approve ? null : this._disapproveConfirm.bind(this)).then(result => {
      //get vpn record from VpnRecordDetail component
      const vpnRecord = this.refs.recordDetail.getData();
      //get task data
      let task = this.refs['default-form'].getData();
      //set VpnRecord to task
      task.vpnRecord = vpnRecord;
      if (!approve && this.state.disapprovePublicNote){
        //Set dissaprove note to current activity
        task.vpnRecord.currentActivity.publicNote = this.state.disapprovePublicNote;
      }

      this.setState({
        showLoading: true
      }, this.refs['default-form'].processStarted());
      const { taskID} = this.props;
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
            //we need refresh vpn record
            if (json){
              approvalTaskManager.getService().getVpnRecord(json.id).then(response => { return response.json()}).then((vpnRecordJson) => {
                if (vpnRecordJson) {
                  if (!vpnRecordJson.error) {
                    this.context.store.dispatch(vpnRecordManager.receiveEntity(vpnRecordJson.id+'', vpnRecordJson));
                  }
                }
              });
            }
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
    const { readOnly, taskID, task, applicant, vpnRecord} = this.props;
    const { showLoading, approvers, disapprovePublicNote} = this.state;
    let showLoadingInternal = task ? showLoading : true;
    let taskState = task ? task.taskState : null;
    let readOnlyInternal = readOnly || this.state.readOnly;
    if (taskState){
      readOnlyInternal = TaskStateEnum.findSymbolByKey(taskState) === TaskStateEnum.PENDING ? readOnlyInternal : true;
    }
    let isImplementer = SecurityManager.isAdmin(null) || SecurityManager.hasRole(null,VpnRecordProfileDetail.IMPLEMENTER_ROLE_NAME);
    return (
      <div>
        <Helmet title={this.i18n('navigation.menu.task')} />
        <Basic.Confirm ref="confirm"/>
        <Basic.Confirm ref="disapproveConfirm">
          <Basic.AbstractForm ref="disapprove-form" style={{marginTop:'10px'}}>
            <Basic.TextArea componentSpan="col-sm-12" ref="disapprovePublicNote" value={disapprovePublicNote} required placeholder={this.i18n('publicNote_placeholder')}/>
          </Basic.AbstractForm>
        </Basic.Confirm>
        <Basic.PageHeader>{approvalTaskManager.getNiceLabel(task)} <small> {this.i18n('taskDetail')}</small>
      </Basic.PageHeader>
      <Basic.Panel showLoading = {showLoadingInternal}>
        <Basic.PanelHeader text={<span>{approvalTaskManager.getNiceLabel(task)} <small>this.i18n('taskDetail')</small></span>} className="hidden">
        </Basic.PanelHeader>
        <Basic.AbstractForm ref="default-form">
          <Basic.EnumLabel ref="taskState" enum={TaskStateEnum} label={this.i18n('taskState')}/>
          <Basic.LabelWrapper readOnly ref="applicant" label={this.i18n('applicant')} componentSpan="col-sm-5">
            <Advanced.IdentityInfo identity={applicant} showLoading={!applicant} className="no-margin"/>
          </Basic.LabelWrapper>
          <Basic.DateTimePicker readOnly ref="createdDate" label={this.i18n('createdDate')}/>
          <Basic.SelectBox ref="approvers" label={this.i18n('approvers')} service={identityManager.getService()} searchInFields={['lastName', 'name','email']}
            multiSelect={true} readOnly/>
          <VpnRecordDetail ref="recordDetail" vpnRecord={vpnRecord} isImplementer={isImplementer} readOnly={readOnlyInternal}/>
        </Basic.AbstractForm>
        <Basic.PanelFooter>
          <Basic.Button type="button" level="link" onClick={this.context.router.goBack}
            showLoading={showLoadingInternal}>{this.i18n('button.back')}</Basic.Button>
          <Basic.Button type="button" level="danger"onClick={this._resolveTask.bind(this,false)}
            showLoading={showLoadingInternal} disabled={readOnlyInternal}>{this.i18n('disapprove')}</Basic.Button>{' '}
            <Basic.Button type="button" level="success" onClick={this._resolveTask.bind(this,true)}
              showLoading={showLoadingInternal} disabled={readOnlyInternal}>{this.i18n('approve')}</Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </div>
    )
  }
}

VpnApprovalTaskDetail.propTypes = {
  task: PropTypes.object,
  readOnly: PropTypes.bool
}
VpnApprovalTaskDetail.defaultProps = {
  task: null,
  readOnly: false
}

function select(state, component) {

  const { taskID } = component;
  if (approvalTaskManager){
    let task = approvalTaskManager.getEntity(state, taskID);
    let applicant = null;
    let vpnRecord = null;
    if (task){
      applicant = identityManager.getEntity(state, task.userName);
      if (!applicant){
        approvalTaskManager.getService().getApplicant(task.id).then(response => { return response.json()}).then((json) => {
          if (json) {
            if (!json.error) {
              this.context.store.dispatch(identityManager.receiveEntity(json.id+'', json));
            }
          }
        });
      }

      if (task.idVpnRecord){
        vpnRecord = vpnRecordManager.getEntity(state, task.idVpnRecord);
        if (!vpnRecord){
          approvalTaskManager.getService().getVpnRecord(task.id).then(response => { return response.json()}).then((json) => {
            if (json) {
              if (!json.error) {
                this.context.store.dispatch(vpnRecordManager.receiveEntity(json.id+'', json));
              }
            }
          });
        }
      }
    }

    return {
      applicant: applicant,
      task: task,
      vpnRecord: vpnRecord
    }
  }
  return {};
}

export default connect(select)(VpnApprovalTaskDetail);
