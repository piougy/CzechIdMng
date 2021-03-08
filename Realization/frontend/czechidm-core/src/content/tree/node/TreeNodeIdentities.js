import React from 'react';
import _ from 'lodash';
//
import * as Basic from '../../../components/basic';
import { IdentityManager } from '../../../redux';
import SearchParameters from '../../../domain/SearchParameters';
import IdentityTableComponent, { IdentityTable } from '../../identity/IdentityTable';

/**
 * Identities with assigned role
 *
 * @author Radek Tomiška
 */
export default class TreeNodeIdentities extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityManager = new IdentityManager();
  }

  getManager() {
    return this.identityManager;
  }

  getContentKey() {
    return 'content.tree.node.identities';
  }

  getNavigationKey() {
    return 'tree-node-identities';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('treeNodeId', this.props.match.params.entityId);
    const columns = _.difference(IdentityTable.defaultProps.columns, ['username']);
    columns.unshift('entityInfo');
    //
    return (
      <div>
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <IdentityTableComponent
          uiKey="tree-node-identities-table"
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
