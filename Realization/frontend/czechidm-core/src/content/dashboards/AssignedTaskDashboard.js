import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { WorkflowTaskInstanceManager, SecurityManager } from '../../redux';
import TaskInstanceTable from '../task/TaskInstanceTable';

const uiKeyPrefix = 'task-instance-dashboard-';

/**
 * Assigned workflow tasks
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 */
class AssignedTaskDashboard extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.workflowTaskInstanceManager = new WorkflowTaskInstanceManager();
  }

  render() {
    const { identity, userContext, _total, _showLoading } = this.props;

    if (!identity || !SecurityManager.hasAuthority('WORKFLOWTASK_READ')) {
      return null;
    }
    if (identity.id !== userContext.id && !SecurityManager.hasAuthority('WORKFLOWTASK_ADMIN')) {
      return null;
    }
    //
    return (
      <Basic.Div>
        <Basic.ContentHeader
          icon="tasks"
          text={ this.i18n('content.tasks-assigned.assigned') }/>
        {
          (_total || !_showLoading)
          ?
          null
          :
          <Basic.Loading isStatic show />
        }
        <Basic.Alert rendered={ !_total && !_showLoading } level="success" style={{ paddingTop: 15, paddingBottom: 15 }}>
          <Basic.Div style={{ display: 'flex', alignItems: 'center', justifyContent: 'left' }}>
            <Basic.Icon value="fa:check" style={{ marginRight: 10 }} className="fa-2x"/>
            <span>{ this.i18n('content.tasks-assigned.empty.message') }</span>
          </Basic.Div>
        </Basic.Alert>
        <Basic.Panel className={ _total ? '' : 'hidden' }>
          <TaskInstanceTable
            uiKey={ `${ uiKeyPrefix }${ identity.username }` }
            username={ identity.username }
            taskInstanceManager={ this.workflowTaskInstanceManager }
            filterOpened={ false }/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

function select(state, component) {
  const uiKey = `${ uiKeyPrefix }${ component.identity ? component.identity.username : '' }`;
  const ui = state.data.ui[uiKey];
  return {
    userContext: state.security.userContext,
    _total: ui ? ui.total : null,
    _showLoading: ui ? ui.showLoading : null,
    i18nReady: state.config.get('i18nReady')
  };
}

export default connect(select)(AssignedTaskDashboard);
