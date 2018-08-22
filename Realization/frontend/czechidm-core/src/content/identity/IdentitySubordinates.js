import React from 'react';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import IdentityTableComponent, { IdentityTable } from './IdentityTable';

/**
 * Identity's subordinates
 *
 * @author Radek Tomiška
 */
export default class IdentitySubordinates extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityManager = new IdentityManager();
  }

  getManager() {
    return this.identityManager;
  }

  getContentKey() {
    return 'content.identity.subordinates';
  }

  getNavigationKey() {
    return 'profile-subordinates';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('subordinatesFor', this.props.params.entityId).setFilter('includeGuarantees', true);
    const columns = _.difference(IdentityTable.defaultProps.columns, ['username']);
    columns.push('entityInfo');
    //
    return (
      <div className="tab-pane-table-body">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <IdentityTableComponent
          uiKey="subordinates_table"
          identityManager={ this.getManager() }
          filterOpened={ false }
          forceSearchParameters={ forceSearchParameters }
          columns={ columns }
          className="no-margin"/>
      </div>
    );
  }
}
