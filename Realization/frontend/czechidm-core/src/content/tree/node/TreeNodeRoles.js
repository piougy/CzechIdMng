import React from 'react';
import Helmet from 'react-helmet';
import _ from 'lodash';
//
import * as Basic from '../../../components/basic';
import { RoleTreeNodeManager } from '../../../redux';
import SearchParameters from '../../../domain/SearchParameters';
import RoleTreeNodeTableComponent, {RoleTreeNodeTable} from '../../role/RoleTreeNodeTable';

/**
 * Automatic roles - tab on tree node detail
 *
 * @author Radek Tomiška
 */
export default class TreeNodeRoles extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new RoleTreeNodeManager();
  }

  getManager() {
    return this.manager;
  }

  getContentKey() {
    return 'content.tree.node.roles';
  }

  getNavigationKey() {
    return 'tree-node-roles';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('treeNodeId', this.props.params.entityId);
    //
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          {this.i18n('header')}
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <RoleTreeNodeTableComponent
            uiKey="tree-node-roles-table"
            manager={this.getManager()}
            forceSearchParameters={forceSearchParameters}
            columns={ _.without(RoleTreeNodeTable.defaultProps.columns, 'treeNode') }/>
        </Basic.Panel>

      </div>
    );
  }
}
