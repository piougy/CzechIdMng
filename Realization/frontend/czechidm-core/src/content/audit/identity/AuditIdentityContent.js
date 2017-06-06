import React from 'react';
import * as Basic from '../../../components/basic';
import AuditIdentityTable from './AuditIdentityTable';

export default class AuditContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit.identities';
  }

  componentDidMount() {
    this.selectNavigationItems(['audit', 'audits', 'audit-identities']);
  }

  render() {
    return (
      <div>
        <AuditIdentityTable />
      </div>
    );
  }
}

AuditContent.propTypes = {
};

AuditContent.defaultProps = {
};
