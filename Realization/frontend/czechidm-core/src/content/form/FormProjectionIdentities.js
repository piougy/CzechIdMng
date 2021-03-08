import React from 'react';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import IdentityTableComponent, { IdentityTable } from '../identity/IdentityTable';

/**
 * Identities with selected projection.
 *
 * @author Radek Tomiška
 */
export default class FormProjectionIdentities extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityManager = new IdentityManager();
  }

  getManager() {
    return this.identityManager;
  }

  getContentKey() {
    return 'content.form-projections.identities';
  }

  getNavigationKey() {
    return 'form-projection-identities';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('formProjection', this.props.match.params.entityId);
    const columns = _.difference(IdentityTable.defaultProps.columns, ['username']);
    columns.unshift('entityInfo');
    //
    return (
      <Basic.Div>
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <IdentityTableComponent
          uiKey="form-projection-identities-table"
          identityManager={ this.getManager() }
          filterOpened={ false }
          forceSearchParameters={ forceSearchParameters }
          showAddButton={ false }
          columns={ columns }
          className="no-margin"/>
      </Basic.Div>
    );
  }
}
