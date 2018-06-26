import React from 'react';
import Helmet from 'react-helmet';
//
import { Basic } from 'czechidm-core';
import { SystemManager } from '../../redux';
import SystemTable from './SystemTable';

/**
 * Content with table of systems
 *
 * @author Radek Tomiška
 */
export default class Systems extends Basic.AbstractContent {

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

  componentDidMount() {
    this.selectNavigationItem('sys-systems');
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          <Basic.Icon value="link"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <SystemTable uiKey="system_table" manager={this.systemManager} filterOpened/>
        </Basic.Panel>
      </div>
    );
  }
}

Systems.propTypes = {
};
Systems.defaultProps = {
};
