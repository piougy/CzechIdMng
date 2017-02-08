import React from 'react';
//
import * as Basic from '../../components/basic';
import { RoleManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import RoleTable from '../role/RoleTable';

export default class IdentityGarantedRoles extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.roleManager = new RoleManager();
  }

  getManager() {
    return this.roleManager;
  }

  getContentKey() {
    return 'content.identity.garanted-roles';
  }

  getNavigationKey() {
    return 'profile-garanted-roles';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('guarantee', this.props.params.entityId);
    return (
      <div className="tab-pane-table-body">
        { this.renderContentHeader() }

        <Basic.Panel className="no-border last">
          <RoleTable
            uiKey="identity-garanted-role-table"
            roleManager={this.getManager()}
            filterOpened={false}
            showCatalogue={false}
            forceSearchParameters={forceSearchParameters} />
        </Basic.Panel>
      </div>
    );
  }
}
