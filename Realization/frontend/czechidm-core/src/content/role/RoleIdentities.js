import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import IdentityTable from '../identity/IdentityTable';

/**
 * Identities with assigned role
 *
 * @author Radek Tomiška
 */
export default class RoleIdentities extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityManager = new IdentityManager();
  }

  getManager() {
    return this.identityManager;
  }

  getContentKey() {
    return 'content.role.identities';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('role-identities', this.props.params);
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('role', this.props.params.entityId);
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          {this.i18n('header')}
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <IdentityTable
            uiKey="role-identities-table"
            identityManager={this.getManager()}
            filterOpened={false}
            forceSearchParameters={forceSearchParameters}
            showAddButton={false}/>
        </Basic.Panel>
      </div>
    );
  }
}
