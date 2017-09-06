import React from 'react';
import Helmet from 'react-helmet';
//
import { Basic, Domain } from 'czechidm-core';
import { Managers, SystemTable } from 'czechidm-acc';


export default class VsSystems extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.systemManager = new Managers.SystemManager();
  }

  getManager() {
    return this.systemManager;
  }

  getContentKey() {
    return 'vs:content.vs-systems';
  }

  componentDidMount() {
    this.selectNavigationItem('vs-systems');
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
          <SystemTable
            uiKey="vs_system_table"
            manager={this.systemManager}
            forceSearchParameters={new Domain.SearchParameters().setFilter('virtual', true)}
            filterOpened={false}/>
        </Basic.Panel>
      </div>
    );
  }
}

VsSystems.propTypes = {
};
VsSystems.defaultProps = {
};
