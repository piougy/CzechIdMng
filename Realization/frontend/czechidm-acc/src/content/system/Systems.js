import React from 'react';
import { connect } from 'react-redux';
//
import { Basic } from 'czechidm-core';
import { SystemManager } from '../../redux';
import SystemTable from './SystemTable';
import SystemWizard from '../wizard/SystemWizard';

/**
 * Content with table of systems
 *
 * @author Radek Tomiška
 */
class Systems extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.systemManager = new SystemManager();
    this.state = {};
  }

  getManager() {
    return this.systemManager;
  }

  getContentKey() {
    return 'acc:content.systems';
  }

  getNavigationKey() {
    return 'sys-systems';
  }

  closeWizard(finished, wizardContext) {
    this.setState({
      showWizard: false
    }, () => {
      if (wizardContext && wizardContext.entity) {
        this.context.history.push(`/system/${wizardContext.entity.id}/detail`);
      }
    });
  }

  showWizardDetail(entity) {
    this.setState({
      showWizard: true
    });
  }


  render() {
    const { match, location } = this.props;
    const { showWizard } = this.state;

    if (showWizard) {
      return (
        <SystemWizard
          show={showWizard}
          closeWizard={this.closeWizard.bind(this)}
          match={match}
          location={location}/>
      );
    }
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <SystemTable
            uiKey="system_table"
            manager={this.systemManager}
            match={this.props.match}
            location={this.props.location}
            filterOpened
            showWizardDetail={this.showWizardDetail.bind(this)}/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady')
  };
}

export default connect(select, null, null, { forwardRef: true })(Systems);
