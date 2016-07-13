'use strict';

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import { WorkflowHistoricProcessInstanceManager, WorkflowHistoricTaskInstanceManager} from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import _ from 'lodash';

/**
* Workflow process historic detail
*/
const workflowHistoricProcessInstanceManager = new WorkflowHistoricProcessInstanceManager();
const workflowHistoricTaskInstanceManager = new WorkflowHistoricTaskInstanceManager();

class HistoricProcessInstanceDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {};
  }

  getContentKey() {
    return 'content.workflow.history.process';
  }

  componentDidMount() {
    const { historicProcessInstanceId } = this.props.params;
    this.context.store.dispatch(workflowHistoricProcessInstanceManager.fetchEntityIfNeeded(historicProcessInstanceId));
    this.selectNavigationItem('workflow-historic-processes');
    workflowHistoricProcessInstanceManager.getService().downloadDiagram(historicProcessInstanceId, this.reciveDiagram.bind(this));
  }

  reciveDiagram(blob){
    var objectURL = URL.createObjectURL(blob);
    this.setState({diagramUrl:objectURL})
  }

  _showFullDiagram(){
    this.setState({showModalDiagram:true});
  }

  _closeModalDiagram(){
    this.setState({showModalDiagram:false});
  }

  render() {
    const {showLoading, diagramUrl, showModalDiagram} = this.state;
    const {_historicProcess} = this.props;
    const { historicProcessInstanceId } = this.props.params;

    let showLoadingInternal = showLoading || !_historicProcess;
    let force = new SearchParameters();
    force = force.setFilter('processInstanceId', historicProcessInstanceId);
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.PageHeader>
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel showLoading={showLoadingInternal}>
          <Basic.AbstractForm ref="form" data={_historicProcess} readOnly>
            <Basic.TextField ref="name" label={this.i18n('name')}/>
            <Basic.TextField ref="id" label={this.i18n('id')}/>
            <Basic.DateTimePicker ref="startTime" label={this.i18n('startTime')}/>
            <Basic.DateTimePicker ref="endTime" label={this.i18n('endTime')}/>
            <Basic.TextArea ref="deleteReason" label={this.i18n('deleteReason')}/>
          </Basic.AbstractForm>
          <Basic.PanelFooter>
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>
              {this.i18n('button.back')}
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
        <Basic.Panel>
          <Basic.PanelHeader>
            {this.i18n('tasks')}
          </Basic.PanelHeader>
          <Advanced.Table
            ref="tableTasks"
            uiKey="table-tasks"
            forceSearchParameters={force}
            manager={workflowHistoricTaskInstanceManager}>
            <Advanced.Column property="name" sort={false} face="text"/>
            <Advanced.Column property="assignee" sort={false} face="text"/>
            <Advanced.Column property="createTime" sort={true} face="datetime"/>
            <Advanced.Column property="endTime" sort={true} face="datetime"/>
            <Advanced.Column property="deleteReason" sort={false} face="text"/>
          </Advanced.Table>
        </Basic.Panel>
        <Basic.Panel showLoading={!diagramUrl}>
          <Basic.PanelHeader>
            {this.i18n('diagram')}  <div className="pull-right">
            <Basic.Button type="button" className="btn-sm" level="success" onClick={this._showFullDiagram.bind(this)}>
              <Basic.Icon icon="fullscreen"/>
            </Basic.Button>
          </div>
        </Basic.PanelHeader>
        <div style={{textAlign:'center', marginBottom:'40px'}}>
          <img style={{maxWidth:'70%'}} src={diagramUrl}/>
        </div>
      </Basic.Panel>
      <Basic.Modal show={showModalDiagram} dialogClassName='modal-large' onHide={this._closeModalDiagram.bind(this)} style={{width: '90%'}} keyboard={!diagramUrl}>
        <Basic.Modal.Header text={this.i18n('fullscreenDiagram')}/>
        <Basic.Modal.Body style={{overflow: 'scroll'}}>
          <img src={diagramUrl}/>
        </Basic.Modal.Body>
        <Basic.Modal.Footer>
          <Basic.Button level="link" disabled={showLoading} onClick={this._closeModalDiagram.bind(this)}>{this.i18n('button.close')}</Basic.Button>
        </Basic.Modal.Footer>
      </Basic.Modal>
    </div>
  );
}
}

HistoricProcessInstanceDetail.propTypes = {
}
HistoricProcessInstanceDetail.defaultProps = {

}

function select(state, component) {
  const { historicProcessInstanceId } = component.params;
  let historicProcess = workflowHistoricProcessInstanceManager.getEntity(state, historicProcessInstanceId);
  return {
    _historicProcess: historicProcess
  }
}

export default connect(select, null, null, { withRef: true})(HistoricProcessInstanceDetail);
