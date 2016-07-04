'use strict';

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import { WorkflowHistoricProcessInstanceManager } from '../../redux';
import _ from 'lodash';

/**
* Workflow process historic detail
*/
const workflowHistoricProcessInstanceManager = new WorkflowHistoricProcessInstanceManager();

class HistoricProcessInstanceDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {};
  }

  getContentKey() {
    return 'content.workflow.history.process.detail';
  }

  componentDidMount() {
    const { historicProcessInstanceId } = this.props.params;
    this.context.store.dispatch(workflowHistoricProcessInstanceManager.fetchEntityIfNeeded(historicProcessInstanceId));
    this.selectNavigationItem('workflow-historic-processes');
  }

  render() {
    const {showLoading} = this.state;
    const {_historicProcess} = this.props;
    let showLoadingInternal = showLoading || !_historicProcess;
    console.log("dddddddddddddddddd", _historicProcess, showLoadingInternal);

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
            <Basic.DateTimePicker ref="starTime" label={this.i18n('starTime')}/>
            <Basic.DateTimePicker ref="endTime" label={this.i18n('endTime')}/>
            <Basic.TextArea ref="deleteReason" label={this.i18n('deleteReason')}/>
          </Basic.AbstractForm>
          <Basic.PanelFooter>
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>
              {this.i18n('button.back')}
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
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
