import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import SearchParameters from '../../domain/SearchParameters';
import RoleCatalogueRoleTable from './RoleCatalogueRoleTable';

/**
 * Role catalogues - role assigned to role catalogue
 *
 * @author Radek Tomiška
 */
export default class RoleCatalogueRoles extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.role.catalogues';
  }

  getNavigationKey() {
    return 'role-catalogue-roles';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('roleId', this.props.params.entityId);
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.ContentHeader icon="fa:list-alt" text={ this.i18n('header') } style={{ marginBottom: 0 }}/>
        <RoleCatalogueRoleTable
          uiKey="role-catalogue-role-table"
          forceSearchParameters={ forceSearchParameters }
          className="no-margin"/>
      </div>
    );
  }
}
