import React from 'react';
import Helmet from 'react-helmet';
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
    this.selectNavigationItems(['audit', 'audit-entities']);
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          <Basic.Icon value="eye-open"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <AuditTable />
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
