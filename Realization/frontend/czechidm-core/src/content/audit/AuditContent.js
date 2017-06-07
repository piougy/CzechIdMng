import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import AuditTable from './AuditTable';

class AuditContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit';
  }

  componentDidMount() {
    this.selectNavigationItems(['audit', 'audits', 'audit-entities']);
  }

  render() {
    return (
      <div>
        <AuditTable />
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
