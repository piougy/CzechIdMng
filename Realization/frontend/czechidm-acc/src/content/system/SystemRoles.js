import React from 'react';
//
import { Basic } from 'czechidm-core';
import { RoleManager } from 'czechidm-core/src/redux';
import SearchParameters from 'czechidm-core/src/domain/SearchParameters';
import RoleTable from 'czechidm-core/src/content/role/RoleTable';

const uiKey = 'system-role-table';
/**
 * Table to display roles, assigned to system
 *
 * @author Radek Tomiška
 */
export default class IdentityGarantedRoles extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.roleManager = new RoleManager();
  }

  getManager() {
    return this.roleManager;
  }

  getContentKey() {
    return 'acc:content.systemRoles';
  }

  getNavigationKey() {
    return 'system-roles';
  }

  render() {
    const { entityId } = this.props.params;
    const forceSearchParameters = new SearchParameters().setFilter('systemId', entityId);
    return (
      <div className="tab-pane-table-body">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }
        <Basic.Panel className="no-border last">
          <RoleTable
            uiKey={uiKey}
          roleManager={this.getManager()}
            filterOpened={false}
            showCatalogue={false}
            forceSearchParameters={forceSearchParameters} />
        </Basic.Panel>
      </div>
    );
  }
}
