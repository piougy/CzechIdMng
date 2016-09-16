import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { WorkflowHistoricProcessInstanceManager } from '../../redux';
import HistoricProcessInstanceTable from './HistoricProcessInstanceTable';

/**
 * List of instances historic processes
 */
class HistoricProcessIntances extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.workflowHistoricProcessInstanceManager = new WorkflowHistoricProcessInstanceManager();
  }

  getContentKey() {
    return 'content.workflow.history.processes';
  }

  componentDidMount() {
    this.selectNavigationItem('workflow-historic-processes');
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <HistoricProcessInstanceTable uiKey="historic_process_instance_table" workflowHistoricProcessInstanceManager={this.workflowHistoricProcessInstanceManager} filterOpened={false}/>
        </Basic.Panel>

      </div>
    );
  }
}

HistoricProcessIntances.propTypes = {
};
HistoricProcessIntances.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(HistoricProcessIntances);
