import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import IdentityTable from './IdentityTable';

/**
 * List of identities
 */
class Identities extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityManager = new IdentityManager();
  }

  getContentKey() {
    return 'content.users';
  }

  componentDidMount() {
    this.selectNavigationItem('users');
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('content.identities.title')} />

        <Basic.PageHeader>
          {this.i18n('content.identities.header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <IdentityTable uiKey="identity_table" identityManager={this.identityManager} filterOpened />
        </Basic.Panel>

      </div>
    );
  }
}

Identities.propTypes = {
};
Identities.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(Identities);
