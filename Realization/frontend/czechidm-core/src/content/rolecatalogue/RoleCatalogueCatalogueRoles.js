import React from 'react';
//
import * as Basic from '../../components/basic';
import { RoleManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import RoleCatalogueRoleTable from '../role/RoleCatalogueRoleTable';

/**
 * Role catalogue - role in catalogue item.
 *
 * @author Radek Tomiška
 * @since 10.5.0
 */
export default class RoleCatalogueCatalogueRoles extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.roleManager = new RoleManager();
  }

  getManager() {
    return this.roleManager;
  }

  getContentKey() {
    return 'content.roleCatalogues.roles';
  }

  getNavigationKey() {
    return 'role-catalogue-catalogue-roles';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('roleCatalogueId', this.props.match.params.entityId);
    //
    return (
      <Basic.Div className="tab-pane-table-body">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <RoleCatalogueRoleTable
          uiKey="role-catalogue-catalogue-role-table"
          forceSearchParameters={ forceSearchParameters }
          className="no-margin"
          match={ this.props.match }
          columns={[ 'role' ]}/>
      </Basic.Div>
    );
  }
}
