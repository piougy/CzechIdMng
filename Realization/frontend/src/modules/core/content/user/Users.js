import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../../../components/basic';
import { IdentityManager } from '../../redux';
import UserTable from './UserTable';

/**
 * Users list
 */
class Users extends Basic.AbstractContent {

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
        <Helmet title={this.i18n('content.users.title')} />

        <Basic.PageHeader>
          {this.i18n('content.users.header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <UserTable uiKey="identity_table" identityManager={this.identityManager} filterOpened />
        </Basic.Panel>

      </div>
    );
  }
}

Users.propTypes = {
};
Users.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(Users);
