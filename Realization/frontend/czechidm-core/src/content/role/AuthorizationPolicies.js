import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import SearchParameters from '../../domain/SearchParameters';
import AuthorizationPolicyTable from './AuthorizationPolicyTable';

/**
 * Automatic roles - tab on role detail.
 *
 * @author Radek Tomiška
 */
export default class AuthorizationPolicies extends Basic.AbstractContent {

  getContentKey() {
    return 'content.role.authorization-policies';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('role-authorization-policies', this.props.match.params);
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('roleId', this.props.match.params.entityId);
    //
    return (
      <Basic.Div>
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <Basic.Panel className="no-border last">
          <AuthorizationPolicyTable
            uiKey="role-authorization-policies-table"
            forceSearchParameters={ forceSearchParameters }
            match={ this.props.match }
            className="no-margin"/>
        </Basic.Panel>

      </Basic.Div>
    );
  }
}
