import React from 'react';
//
import * as Basic from '../../components/basic';
import ComponentService from '../../services/ComponentService';

export default class PasswordChange extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.componentService = new ComponentService();
  }

  componentDidMount() {
    this.selectSidebarItem('profile-password');
  }

  render() {
    const PasswordChangeContent = this.componentService.getComponent('password-change-content');
    //
    return (
      <PasswordChangeContent params={this.props.params}/>
    );
  }
}
