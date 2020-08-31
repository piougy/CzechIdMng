import React from 'react';
//
import * as Basic from '../../../components/basic';
import AuthorizationPolicyTable from '../../role/AuthorizationPolicyTable';
import SearchParameters from '../../../domain/SearchParameters';

/**
 * Identity authorization policies.
 *
 * @author Radek Tomiška
 */
export default class IdentityAuthorizationPolicies extends Basic.AbstractContent {

  getContentKey() {
    return 'content.identity.authorization-policies';
  }

  getNavigationKey() {
    return 'profile-authorization-policies';
  }

  componentDidMount() {
    super.componentDidMount();
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('identityId', this.props.match.params.entityId);
    //
    return (
      <Basic.Div>
        <AuthorizationPolicyTable
          uiKey="identity-authorization-policies-table"
          forceSearchParameters={ forceSearchParameters }
          match={ this.props.match }/>
      </Basic.Div>
    );
  }
}
