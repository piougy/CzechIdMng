import React from 'react';
import * as Basic from '../../../components/basic';
import AuditIdentityRolesTable from './AuditIdentityRolesTable';
import Helmet from 'react-helmet';

/**
 * Audit for identity roles
 *
 * @author Ondřej Kopr
 */
export default class AuditIdentityRolesContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit';
  }

  componentDidMount() {
    this.selectNavigationItems(['audit', 'audits', 'audit-identity-roles']);
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title-identity-roles')} />
        <AuditIdentityRolesTable uiKey="audit-table-role-identities" />
      </div>
    );
  }
}

AuditIdentityRolesContent.propTypes = {
};

AuditIdentityRolesContent.defaultProps = {
};
