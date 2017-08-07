import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import DecisionButtons from '../DecisionButtons';
import DynamicTaskDetail from '../DynamicTaskDetail';
import { RoleManager, TreeNodeManager } from '../../../redux';
import RecursionTypeEnum from '../../../enums/RecursionTypeEnum';

const roleManager = new RoleManager();
const treeNodeManager = new TreeNodeManager();

/**
 * Custom task detail for approve automatic role
 *
 * @author Radek Tomiška
 */
class AutomaticRoleTaskDetail extends DynamicTaskDetail {

  getContentKey() {
    return 'content.task.instance';
  }

  componentDidMount() {
    super.componentDidMount();
  }

  render() {
    const {task, taskManager} = this.props;
    const { showLoading} = this.state;
    const showLoadingInternal = task ? showLoading : true;

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
            {this._getApplicantAndRequester(task)}
            <Basic.DateTimePicker ref="taskCreated" readOnly label={this.i18n('createdDate')}/>
          </Basic.AbstractForm>
        </Basic.Panel>
        <Basic.Panel showLoading = {showLoadingInternal}>
          <Basic.PanelHeader text={<small>{ taskManager.getNiceLabel(task) }</small>}/>
          <Basic.AbstractForm data={ task.variables.entityEvent.content } readOnly style={{ padding: '15px 15px 0px 15px' }}>
            <Basic.SelectBox
              ref="role"
              label={ this.i18n('entity.RoleTreeNode.role')}
              manager={ roleManager }/>
            <Basic.SelectBox
              ref="treeNode"
              label={ this.i18n('entity.RoleTreeNode.treeNode')}
              manager={ treeNodeManager }/>
            <Basic.EnumSelectBox
              ref="recursionType"
              enum={RecursionTypeEnum}
              label={this.i18n('entity.RoleTreeNode.recursionType')}
              required/>
          </Basic.AbstractForm>
          <Basic.PanelFooter>
            <DecisionButtons task={task} onClick={this._validateAndCompleteTask.bind(this)}/>
          </Basic.PanelFooter>
        </Basic.Panel>
      </div>
    );
  }
}

AutomaticRoleTaskDetail.propTypes = {
};

AutomaticRoleTaskDetail.defaultProps = {
};
function select() {
  return {
  };
}

export default connect(select, null, null, { withRef: true })(AutomaticRoleTaskDetail);
