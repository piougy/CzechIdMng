import React from 'react';
import * as Basic from '../../../components/basic';
import AuditIdentityTable from './AuditIdentityTable';
import Helmet from 'react-helmet';

export default class AuditContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit';
  }

  componentDidMount() {
    this.selectNavigationItems(['audit', 'audits', 'audit-identities']);
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title-identities')} />
        <AuditIdentityTable />
      </div>
    );
  }
}

AuditContent.propTypes = {
};

AuditContent.defaultProps = {
};
