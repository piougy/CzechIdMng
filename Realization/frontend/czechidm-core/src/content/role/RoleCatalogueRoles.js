import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import SearchParameters from '../../domain/SearchParameters';
import RoleCatalogueRoleTable from './RoleCatalogueRoleTable';

/**
 * Role catalogues - role assigned to role catalogue.
 *
 * @author Radek Tomiška
 */
export default class RoleCatalogueRoles extends Basic.AbstractContent {


  getContentKey() {
    return 'content.role.catalogues';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('role-catalogue-roles', this.props.match.params);
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('roleId', this.props.match.params.entityId);
    //
    return (
      <Basic.Div className="tab-pane-table-body">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <RoleCatalogueRoleTable
          uiKey="role-catalogue-role-table"
          forceSearchParameters={ forceSearchParameters }
          className="no-margin"
          match={ this.props.match }
          columns={[ 'roleCatalogue' ]}/>
      </Basic.Div>
    );
  }
}
