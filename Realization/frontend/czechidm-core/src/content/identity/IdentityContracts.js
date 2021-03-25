import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Domain from '../../domain';
import { SecurityManager } from '../../redux';
import ContractTable from './contract/ContractTable';
import ContractSlices from './ContractSlices';

/**
 * Identity's work positions - reference to tree structures and garants.
 *
 * @author Radek Tomiška
 */
export default class IdentityContracts extends Advanced.AbstractTableContent {

  getContentKey() {
    return 'content.identity.identityContracts';
  }

  getNavigationKey() {
    return 'profile-contracts';
  }

  reload() {
    this.refs.table.reload();
  }

  render() {
    const { entityId } = this.props.match.params;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('identity', entityId);
    //
    return (
      <Basic.Div>
        { this.renderContentHeader({ style: { marginBottom: 0 } }) }

        <ContractTable
          ref="table"
          uiKey={ `identity-contracts-${ entityId }` }
          forceSearchParameters={ forceSearchParameters }
          className="no-margin"
          match={ this.props.match }/>

        <ContractSlices
          rendered={ SecurityManager.hasAuthority('CONTRACTSLICE_READ') }
          match={ this.props.match }
          reloadExternal={ this.reload.bind(this) }/>
      </Basic.Div>
    );
  }
}
