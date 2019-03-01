import React from 'react';
import * as Basic from '../../../components/basic';
import AuditIdentityLoginTable from './AuditIdentityLoginTable';
import Helmet from 'react-helmet';

/**
 * Audit for identity login
 *
 * @author Ondřej Kopr
 */
export default class AuditIdentityLoginContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit';
  }

  componentDidMount() {
    this.selectNavigationItems(['audit', 'audits', 'audit-identity-login']);
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title-identity-login')} />
        <AuditIdentityLoginTable uiKey="audit-table-login-identities" />
      </div>
    );
  }
}

AuditIdentityLoginContent.propTypes = {
};

AuditIdentityLoginContent.defaultProps = {
};
