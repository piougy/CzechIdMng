import React from 'react';
import Helmet from 'react-helmet';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import SearchParameters from '../../domain/SearchParameters';
import RoleTreeNodeTableComponent, { RoleTreeNodeTable } from './RoleTreeNodeTable';

/**
 * Automatic roles by tree structure - tab on role detail.
 *
 * @author Radek Tomiška
 */
export default class RoleTreeNodes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.role.tree-nodes';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('role-automatic-role-tree', this.props.match.params);
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('roleId', this.props.match.params.entityId);
    //
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />
        <RoleTreeNodeTableComponent
          uiKey="role-tree-nodes-table"
          forceSearchParameters={ forceSearchParameters }
          columns={ _.without(RoleTreeNodeTable.defaultProps.columns, 'role') }/>
      </Basic.Div>
    );
  }
}
