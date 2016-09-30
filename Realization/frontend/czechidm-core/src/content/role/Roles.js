import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import { RoleManager } from '../../redux';
import RoleTable from './RoleTable';

/**
 * List of roles
 */
export default class Roles extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.roleManager = new RoleManager();
  }

  getContentKey() {
    return 'content.roles';
  }

  componentDidMount() {
    this.selectNavigationItem('roles');
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          <Basic.Icon value="fa:group"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <RoleTable uiKey="role_table" roleManager={this.roleManager} filterOpened={false}/>
        </Basic.Panel>

      </div>
    );
  }
}

Roles.propTypes = {
};
Roles.defaultProps = {
};
