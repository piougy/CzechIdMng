import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../../components/basic';
import AuditIdentityTable from './AuditIdentityTable';

class AuditContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit.identities';
  }

  componentDidMount() {
    this.selectNavigationItems(['audit', 'audit-identities']);
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          <Basic.Icon value="eye-close"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <AuditIdentityTable />
        </Basic.Panel>
      </div>
    );
  }
}

AuditContent.propTypes = {
};

AuditContent.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(AuditContent);
