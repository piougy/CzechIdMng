import React from 'react';
import Helmet from 'react-helmet';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import IdentityTableComponent, { IdentityTable } from '../identity/IdentityTable';

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
    return 'role-identities';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('role', this.props.params.entityId);
    const columns = _.difference(IdentityTable.defaultProps.columns, ['username']);
    columns.push('entityInfo');
    //
    return (
      <div>
        <Helmet title={ this.i18n('title') } />

        <Basic.ContentHeader icon="fa:group" text={ this.i18n('header') } style={{ marginBottom: 0 }}/>
        <IdentityTableComponent
          uiKey="role-identities-table"
          identityManager={ this.getManager() }
          filterOpened={ false }
          forceSearchParameters={ forceSearchParameters }
          showAddButton={ false }
          columns={ columns }
          className="no-margin"/>
      </div>
    );
  }
}
