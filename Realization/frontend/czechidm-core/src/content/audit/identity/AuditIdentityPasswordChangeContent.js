import React from 'react';
import * as Basic from '../../../components/basic';
import AuditIdentityPasswordChangeTable from './AuditIdentityPasswordChangeTable';
import Helmet from 'react-helmet';

/**
 * Audit for identity password change
 *
 * @author Ondřej Kopr
 */
export default class AuditIdentityPasswordChangeContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit';
  }

  componentDidMount() {
    this.selectNavigationItems(['audit', 'audits', 'audit-identity-password-change']);
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title-identity-password-change')} />
        <AuditIdentityPasswordChangeTable uiKey="audit-table-password-change-identities" />
      </div>
    );
  }
}

AuditIdentityPasswordChangeContent.propTypes = {
};

AuditIdentityPasswordChangeContent.defaultProps = {
};
