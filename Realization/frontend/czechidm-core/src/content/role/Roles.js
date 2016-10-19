import React from 'react';
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

  getNavigationKey() {
    return 'roles';
  }

  render() {
    return (
      <div>
        {this.renderPageHeader()}

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
