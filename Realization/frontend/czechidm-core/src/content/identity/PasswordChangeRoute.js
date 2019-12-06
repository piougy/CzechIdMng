import React from 'react';
//
import * as Basic from '../../components/basic';
import ComponentService from '../../services/ComponentService';

/**
 * @author Ondřej Kopr
 */
export default class PasswordChange extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.componentService = new ComponentService();
  }

  componentDidMount() {
    this.selectNavigationItems(['identities', 'identity-profile', 'profile-password', 'profile-password-change']);
  }

  render() {
    const PasswordChangeContent = this.componentService.getComponent('password-change-content');
    //
    return (
      <PasswordChangeContent match={ this.props.match }/>
    );
  }
}
