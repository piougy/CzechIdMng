import React from 'react';
import { connect } from 'react-redux';
//
import { Basic } from 'czechidm-core';
import { SystemManager } from '../../redux';
import SystemTable from './SystemTable';

/**
 * Content with table of systems
 *
 * @author Radek Tomiška
 */
class Systems extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.systemManager = new SystemManager();
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

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <SystemTable uiKey="system_table" manager={ this.systemManager } filterOpened/>
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
