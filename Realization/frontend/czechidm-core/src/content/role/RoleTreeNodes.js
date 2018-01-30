import React from 'react';
import Helmet from 'react-helmet';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import SearchParameters from '../../domain/SearchParameters';
import RoleTreeNodeTableComponent, {RoleTreeNodeTable} from './RoleTreeNodeTable';

/**
 * Automatic roles - tab on role detail
 *
 * @author Radek Tomiška
 */
export default class RoleTreeNodes extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.role.tree-nodes';
  }

  getNavigationKey() {
    return 'role-tree-nodes';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('roleId', this.props.params.entityId);
    //
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          {this.i18n('header')}
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <RoleTreeNodeTableComponent
            uiKey="role-tree-nodes-table"
            forceSearchParameters={ forceSearchParameters }
            columns={ _.without(RoleTreeNodeTable.defaultProps.columns, 'role') }/>
        </Basic.Panel>

      </div>
    );
  }
}
