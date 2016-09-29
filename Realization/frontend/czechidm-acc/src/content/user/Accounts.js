import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic } from 'czechidm-core';


class Accounts extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'acc:content.user.accounts';
  }

  componentDidMount() {
    this.selectSidebarItem('profile-accounts');
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        acc user
      </div>
    );
  }
}

Accounts.propTypes = {
};
Accounts.defaultProps = {
};

function select(state, component) {
  return {
  };
}

export default connect(select)(Accounts);
