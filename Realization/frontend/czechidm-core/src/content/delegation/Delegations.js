import React from 'react';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import DelegationTable from './DelegationTable';

/**
 * Delegations - list of delegations with link to the goal of delegation.
 *
 * @author Vít Švanda
 */
export default class DelegationIdentities extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityManager = new IdentityManager();
  }

  getManager() {
    return this.identityManager;
  }

  getContentKey() {
    return 'content.delegation-definitions.delegations';
  }

  getNavigationKey() {
    if (this.isIncluded()) {
      return 'identity-delegations';
    }
    return 'delegations';
  }

  /**
   * Returns true, if is component included in under other route (for example is tab on identity).
   */
  isIncluded() {
    // Entity ID indicates ID of identity ... component is inculded.
    return !!this.props.match.params.entityId;
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('delegationDefinitionId', this.props.match.params.delegationId);
    const style = this.isIncluded() ? { paddingRight: 15, paddingLeft: 15 } : {marginBottom: 0};
    //
    return (
      <Basic.Div>
        { this.isIncluded() ? null : this.renderContentHeader({style}) }

        <DelegationTable
          uiKey="delegation-definition-delegation-table"
          columns={['ownerId', 'created']}
          filterOpened={false}
          forceSearchParameters={ forceSearchParameters }
          className="no-margin"/>
      </Basic.Div>
    );
  }
}
