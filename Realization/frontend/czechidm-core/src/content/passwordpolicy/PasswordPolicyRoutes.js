import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import PasswordPolicyBasic from './PasswordPolicyBasic';

/**
 * Default content (routes diff) for password policies
 */

class PasswordPolicyRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.passwordPolicies';
  }

  /**
   * Method check if exist params new
   */
  _getIsNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    return (
      <div>
        {
          this._getIsNew()
          ?
          <Helmet title={this.i18n('create.title')} />
          :
          <Helmet title={this.i18n('edit.title')} />
        }
        <Basic.PageHeader>
          {
            this._getIsNew()
            ?
            this.i18n('create.header')
            :
            this.i18n('edit.header')
          }
        </Basic.PageHeader>
        {
          this._getIsNew()
          ?
          <PasswordPolicyBasic params={this.props.params} isNew />
          :
          <Advanced.TabPanel position="left" parentId="password-policies" params={this.props.params}>
            {this.props.children}
          </Advanced.TabPanel>
        }
      </div>
    );
  }
}

PasswordPolicyRoutes.propTypes = {
};
PasswordPolicyRoutes.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(PasswordPolicyRoutes);
