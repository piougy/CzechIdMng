import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic } from 'czechidm-core';

class SystemConnectorContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'acc:content.system.connector';
  }

  componentDidMount() {
    this.selectNavigationItems(['sys-systems', 'system-connector']);
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          TODO:<br />
          - vyber connector serveru<br />
          - vyber typu konector frameworku a typu konecctoru<br />
          - po vyberu typu konektoru - formular s nastavenim<br />
          - wizard?
        </Basic.Panel>
      </div>
    );
  }
}

SystemConnectorContent.propTypes = {
};
SystemConnectorContent.defaultProps = {
};

function select(/* state*/) {
  return {
  };
}

export default connect(select)(SystemConnectorContent);
