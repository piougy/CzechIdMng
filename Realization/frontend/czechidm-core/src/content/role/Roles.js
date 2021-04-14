import React from 'react';
//
import * as Basic from '../../components/basic';
import { RoleManager } from '../../redux';
import RoleTable from './RoleTable';

/**
 * List of roles.
 *
 * @author Radek Tomiška
 */
export default class Roles extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
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
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <RoleTable uiKey="role_table" roleManager={ this.roleManager }/>
        </Basic.Panel>

      </Basic.Div>
    );
  }
}

Roles.propTypes = {
};
Roles.defaultProps = {
};
